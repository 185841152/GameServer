package com.net.engine.sessions;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.net.engine.config.EngineConfiguration;
import com.net.engine.core.NetEngine;
import com.net.engine.events.Event;
import com.net.engine.exceptions.BitSwarmEngineRuntimeException;
import com.net.engine.exceptions.SessionReconnectionException;
import com.net.engine.service.IService;
import com.net.engine.util.scheduling.Task;
import com.net.engine.websocket.IWebSocketChannel;

import io.netty.channel.Channel;

public class DefaultSessionManager implements ISessionManager {
//	private static final String SESSION_CLEANING_TASK_ID = "SessionCleanerTask";
	public static final int SESSION_CLEANING_INTERVAL_SECONDS = 10;
	private static ISessionManager __instance__;
	private Logger logger;
	private ConcurrentMap<String, List<ISession>> sessionsByNode;
	private ConcurrentMap<Integer, ISession> sessionsById;
	private NetEngine engine = null;

	private EngineConfiguration config = null;
	private final List<ISession> localSessions;
	private final ConcurrentMap<Integer, ISession> localSessionsById;
	private final ConcurrentMap<Channel, ISession> localSessionsByConnection;
	private String serviceName = "DefaultSessionManager";
	private Task sessionCleanTask;
//	private Scheduler systemScheduler;
	IReconnectionManager reconnectionManager;
	private int highestCCS = 0;
	private IPacketQueuePolicy packetQueuePolicy;

	public static ISessionManager getInstance() {
		if (__instance__ == null) {
			__instance__ = new DefaultSessionManager();
		}
		return __instance__;
	}

	private DefaultSessionManager() {

		if (this.sessionsByNode == null) {
			this.sessionsByNode = new ConcurrentHashMap<String, List<ISession>>();
		}
		if (this.sessionsById == null) {
			this.sessionsById = new ConcurrentHashMap<Integer, ISession>();
		}

		this.localSessions = new ArrayList<ISession>();
		this.localSessionsById = new ConcurrentHashMap<Integer, ISession>();
		this.localSessionsByConnection = new ConcurrentHashMap<Channel, ISession>();

		this.reconnectionManager = new DefaultReconnectionManager(this);
	}

	public void init(Object o) {
		this.engine = NetEngine.getInstance();
		this.config = this.engine.getConfiguration();

		this.logger = LoggerFactory.getLogger(DefaultSessionManager.class);

//		this.systemScheduler = ((Scheduler) this.engine.getServiceByName("scheduler"));
//		this.sessionCleanTask = new Task(SESSION_CLEANING_TASK_ID);
//		this.systemScheduler.addScheduledTask(this.sessionCleanTask, 10, true, new SessionCleaner());
//
//		((IService) this.reconnectionManager).init(this.systemScheduler);
		try {
			Class<?> packetPolicyClass = Class.forName(this.config.getPacketQueuePolicyClass());
			this.packetQueuePolicy = ((IPacketQueuePolicy) packetPolicyClass.newInstance());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void destroy(Object o) {
		this.sessionCleanTask.setActive(false);

		((IService) this.reconnectionManager).destroy(null);

		shutDownLocalSessions();

		this.localSessionsById.clear();
		this.localSessionsByConnection.clear();
	}

	public void publishLocalNode(String nodeId) {
		if (this.sessionsByNode.get(nodeId) != null) {
			throw new IllegalStateException("NodeID already exists in the cluster: " + nodeId);
		}
		this.sessionsByNode.put(nodeId, this.localSessions);
	}

	public void addSession(ISession session) {
		synchronized (this.localSessions) {
			this.localSessions.add(session);
		}

		this.localSessionsById.put(Integer.valueOf(session.getId()), session);
		
		if (session.getType() == SessionType.DEFAULT) {
			this.localSessionsByConnection.put(session.getConnection(), session);
		}

		if (this.config.isClustered()) {
			this.sessionsById.put(Integer.valueOf(session.getId()), session);
		}
		if (this.localSessions.size() > this.highestCCS) {
			this.highestCCS = this.localSessions.size();
		}
		this.logger.info("Session created: " + session + " on Server port: " + session.getServerPort() + " <---> "
				+ session.getClientPort());
	}

	public boolean containsSession(ISession session) {
		return this.localSessionsById.containsValue(session);
	}

	public void removeSession(ISession session) {
		if (session == null) {
			return;
		}

		synchronized (this.localSessions) {
			this.localSessions.remove(session);
		}

		Channel connection = session.getConnection();
		int id = session.getId();

		this.localSessionsById.remove(Integer.valueOf(id));

		if (connection != null) {
			this.localSessionsByConnection.remove(connection);
		}

		if (session.getType() != SessionType.VOID) {
			this.engine.getSocketAcceptor().getConnectionFilter().removeAddress(session.getAddress());
		}

		if (this.config.isClustered()) {
			this.sessionsById.remove(Integer.valueOf(id));
		}

		this.logger.info("Session removed: " + session);
	}

	public ISession removeSession(int id) {
		ISession session = (ISession) this.localSessionsById.get(Integer.valueOf(id));

		if (session != null) {
			removeSession(session);
		}
		return session;
	}

	public ISession removeSession(String hash) {
		throw new UnsupportedOperationException("Not implemented yet!");
	}

	public ISession removeSession(Channel connection) {
		ISession session = getLocalSessionByConnection(connection);

		if (session != null) {
			removeSession(session);
		}
		return session;
	}

	public void onSocketDisconnected(Channel connection) throws IOException {
		ISession session = (ISession) this.localSessionsByConnection.get(connection);

		if (session == null) {
			return;
		}

		this.localSessionsByConnection.remove(connection);
		session.setConnected(false);

		onSocketDisconnected(session);
	}

	public void onSocketDisconnected(ISession session) throws IOException {
		if (session.getReconnectionSeconds() > 0) {
			this.reconnectionManager.onSessionLost(session);
			dispatchSessionReconnectionTryEvent(session);
		} else {
			removeSession(session);
			dispatchLostSessionEvent(session);
		}
	}

	public ISession reconnectSession(ISession tempSession, String sessionToken)
			throws SessionReconnectionException, IOException {
		ISession resumedSession = null;
		try {
			resumedSession = this.reconnectionManager.reconnectSession(tempSession, sessionToken);
		} catch (SessionReconnectionException sre) {
			throw sre;
		}

		this.localSessionsByConnection.put(tempSession.getConnection(), resumedSession);

		tempSession.setConnection(null);

		this.logger.info("Session was resurrected: " + resumedSession + ", using temp Session: " + tempSession + ", "
				+ resumedSession.getReconnectionSeconds());

		return resumedSession;
	}

	public List<ISession> getAllLocalSessions() {
		List<ISession> allSessions = null;

		synchronized (this.localSessions) {
			allSessions = new ArrayList<ISession>(this.localSessions);
		}

		return allSessions;
	}

	public List<ISession> getAllSessions() {
		List<ISession> sessions = null;

		sessions = this.config.isClustered() ? new LinkedList<ISession>(this.sessionsById.values())
				: getAllLocalSessions();

		return sessions;
	}

	public List<ISession> getAllSessionsAtNode(String nodeName) {
		List<ISession> allSessions = null;

		List<ISession> theSessions = this.sessionsByNode.get(nodeName);
		if (theSessions != null) {
			allSessions = new ArrayList<ISession>(theSessions);
		}
		return allSessions;
	}

	public ISession getLocalSessionByHash(String hash) {
		for (ISession session : this.localSessionsById.values()) {
			if (session.getHashId().equals(hash)) {
				return session;
			}
		}
		return null;
	}

	public ISession getLocalSessionById(int id) {
		return (ISession) this.localSessionsById.get(Integer.valueOf(id));
	}

	public ISession getSessionByHash(String hash) {
		throw new UnsupportedOperationException("Not implemented yet!");
	}

	public ISession getLocalSessionByConnection(Channel connection) {
		return (ISession) this.localSessionsByConnection.get(connection);
	}

	public ISession getSessionById(int id) {
		return (ISession) this.sessionsById.get(Integer.valueOf(id));
	}

	public int getHighestCCS() {
		return this.highestCCS;
	}

	public void shutDownLocalSessions() {
		synchronized (this.localSessions) {
			for (Iterator<ISession> it = this.localSessions.iterator(); it.hasNext();) {
				ISession session = it.next();
				it.remove();
				try {
					session.close();
				} catch (IOException e) {
					// this.bootLogger.warn("I/O Error while closing session: "
					// + session);
				}
			}
		}
	}

	public void onNodeLost(String nodeId) {
		List<ISession> nodeSessions = this.sessionsByNode.remove(nodeId);

		if (nodeSessions == null) {
			throw new IllegalStateException("Unable to remove node sessions from cluster. Lost Node ID: " + nodeId);
		}

		synchronized (this.sessionsById) {
			for (ISession session : nodeSessions) {
				this.sessionsById.remove(Integer.valueOf(session.getId()));
			}
		}
	}

	public void clearClusterData() {
		this.sessionsById.clear();
		this.sessionsByNode.clear();
	}

	public String getName() {
		return this.serviceName;
	}

	public void setName(String name) {
		this.serviceName = name;
	}

	public void handleMessage(Object message) {
		throw new UnsupportedOperationException("Not implemented in this class!");
	}

	public ISession createSession(Channel connection) {
		InetSocketAddress iAddr = (InetSocketAddress) connection.remoteAddress();
		String nodeName=iAddr.getHostName();
		
		ISession session = new Session();
		session.setSessionManager(this);
		session.setConnection(connection);
		session.setMaxIdleTime(this.engine.getConfiguration().getDefaultMaxSessionIdleTime());
		session.setNodeId(nodeName);
		session.setType(SessionType.DEFAULT);
		session.setReconnectionSeconds(this.engine.getConfiguration().getGlobalReconnectionSeconds());

		IPacketQueue packetQueue = new NonBlockingPacketQueue(this.engine.getConfiguration().getSessionPacketQueueMaxSize());
		packetQueue.setPacketQueuePolicy(this.packetQueuePolicy);
		session.setPacketQueue(packetQueue);

		return session;
	}

	public ISession createConnectionlessSession() {
		ISession session = new Session();
		session.setSessionManager(this);
		session.setNodeId("");
		session.setType(SessionType.VOID);
		session.setConnected(true);

		return session;
	}

	public ISession createWebSocketSession(Object channel) {
		Session session = new Session();
		session.setSessionManager(this);
		session.setMaxIdleTime(this.engine.getConfiguration().getDefaultMaxSessionIdleTime());
		session.setNodeId("");
		session.setType(SessionType.WEBSOCKET);
		session.setConnected(true);

		IPacketQueue packetQueue = new NonBlockingPacketQueue(
				this.engine.getConfiguration().getSessionPacketQueueMaxSize());
		packetQueue.setPacketQueuePolicy(this.packetQueuePolicy);
		session.setPacketQueue(packetQueue);

		session.setSystemProperty("wsChannel", channel);

		return session;
	}

	public int getLocalSessionCount() {
		return this.localSessions.size();
	}

	public int getNodeSessionCount(String nodeId) {
		List<ISession> nodeSessionList = this.sessionsByNode.get(nodeId);

		if (nodeSessionList == null) {
			throw new BitSwarmEngineRuntimeException(
					"Can't find session count for requested node in the cluster. Node not found: " + nodeId);
		}
		return nodeSessionList.size();
	}

//	private void applySessionCleaning() {
//		if (getLocalSessionCount() > 0) {
//			for (ISession session : getAllLocalSessions()) {
//				if ((session == null) || (session.isFrozen())) {
//					continue;
//				}
//				if (session.isMarkedForEviction()) {
//					terminateSession(session);
//					this.logger.info("Terminated idle logged-in session: " + session);
//				} else {
//					if (!session.isIdle()) {
//						continue;
//					}
//					if (session.isLoggedIn()) {
//						session.setMarkedForEviction();
//
//						dispatchSessionIdleEvent(session);
//					} else {
//						terminateSession(session);
//
//						if (this.logger.isDebugEnabled()) {
//							this.logger.debug("Removed idle session: " + session);
//						}
//					}
//
//				}
//
//			}
//
//		}
//
//		Event event = new Event("sessionIdleCheckComplete");
//		this.engine.dispatchEvent(event);
//	}

	public void terminateSession(ISession session) {
		if (session.getType() == SessionType.DEFAULT) {
			Channel connection = session.getConnection();

			session.setReconnectionSeconds(0);
			try {
				if (connection.isActive()) {
					connection.close();
				}

				session.setConnected(false);
			} catch (Exception err) {
				this.logger.warn("Failed closing connection while removing idle Session: " + session);
			}

		} else if (session.getType() == SessionType.WEBSOCKET) {
			IWebSocketChannel channel = (IWebSocketChannel) session.getSystemProperty("wsChannel");
			channel.close();
			return;
		}

		removeSession(session);

		dispatchLostSessionEvent(session);
	}

	private void dispatchLostSessionEvent(ISession closedSession) {
		Event event = new Event("sessionLost");
		event.setParameter("session", closedSession);
		this.engine.dispatchEvent(event);
	}

//	private void dispatchSessionIdleEvent(ISession idleSession) {
//		Event event = new Event("sessionIdle");
//		event.setParameter("session", idleSession);
//		this.engine.dispatchEvent(event);
//	}

	private void dispatchSessionReconnectionTryEvent(ISession session) {
		Event event = new Event("sessionReconnectionTry");
		event.setParameter("session", session);
		this.engine.dispatchEvent(event);
	}

	public void dump() {
		System.err.println("SESSIONS BY ID: " + this.localSessionsById);
	}

//	private final class SessionCleaner implements ITaskHandler {
//		private SessionCleaner() {
//		}
//
//		public void doTask(Task task) throws Exception {
//			DefaultSessionManager.this.applySessionCleaning();
//		}
//	}
}