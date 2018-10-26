package com.net.engine.sessions;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.net.engine.core.NetEngine;
import com.net.engine.events.Event;
import com.net.engine.exceptions.SessionReconnectionException;
import com.net.engine.service.IService;
import com.net.engine.util.scheduling.ITaskHandler;
import com.net.engine.util.scheduling.Scheduler;
import com.net.engine.util.scheduling.Task;

import io.netty.channel.Channel;

public final class DefaultReconnectionManager implements IService, IReconnectionManager {
	private static final String SERVICE_NAME = "DefaultReconnectionManager";
	private static final String RECONNETION_CLEANING_TASK_ID = "SessionReconnectionCleanerTask";
	private final ISessionManager sessionManager;
	private final Map<String, ISession> frozenSessionsByHash;
	private final Logger logger;
	private Task sessionReconnectionCleanTask;
	private Scheduler systemScheduler;
	private NetEngine engine;
	private boolean allowUnfrozenReconnection = true;

	public DefaultReconnectionManager(ISessionManager sessionManager) {
		this.sessionManager = sessionManager;
		this.logger = LoggerFactory.getLogger(getClass());

		this.frozenSessionsByHash = new ConcurrentHashMap<String, ISession>();
	}

	public void init(Object o) {
		this.engine = NetEngine.getInstance();

		this.systemScheduler = ((Scheduler) o);
		this.sessionReconnectionCleanTask = new Task(RECONNETION_CLEANING_TASK_ID);
		this.systemScheduler.addScheduledTask(this.sessionReconnectionCleanTask, 3, true,
				new ReconnectionSessionCleaner());
	}

	public void destroy(Object o) {
		this.sessionReconnectionCleanTask.setActive(false);

		this.frozenSessionsByHash.clear();
	}

	public String getName() {
		return SERVICE_NAME;
	}

	public void handleMessage(Object message) {
		throw new UnsupportedOperationException("Not supported in this class");
	}

	public void setName(String name) {
		throw new UnsupportedOperationException("Not supported in this class");
	}

	public ISessionManager getSessionManager() {
		return this.sessionManager;
	}

	public void onSessionLost(ISession session) {
		addSession(session);

		session.freeze();
	}

	public ISession getReconnectableSession(String token) {
		return (ISession) this.frozenSessionsByHash.get(token);
	}

	public ISession reconnectSession(ISession tempSession, String prevSessionToken)
			throws SessionReconnectionException {
		Channel connection = tempSession.getConnection();
		ISession session = getReconnectableSession(prevSessionToken);

		if ((session == null) && (this.allowUnfrozenReconnection)) {
			session = this.engine.getSessionManager().getLocalSessionByHash(prevSessionToken);
		}
		if (session == null) {
			dispatchSessionReconnectionFailureEvent(tempSession);
			throw new SessionReconnectionException(
					"Session Reconnection failure. The passed Session is not managed by the ReconnectionManager: "
							+ connection);
		}

		if (!connection.isActive()) {
			throw new SessionReconnectionException(
					"Session Reconnection failure. The new socket is not connected: " + session.toString());
		}

		if (session.isReconnectionTimeExpired()) {
			throw new SessionReconnectionException(
					"Session Reconnection failure. Time expired for Session: " + session.toString());
		}

		session.setConnection(connection);

//		session.setSystemProperty("SessionSelectionKey", tempSession.getSystemProperty("SessionSelectionKey"));

		removeSession(session);

		session.unfreeze();

		dispatchSessionReconnectionSuccessEvent(session);

		this.logger.debug("Reconnection done. Sessions remaining: " + this.frozenSessionsByHash);

		return session;
	}

	private void addSession(ISession session) {
		if (this.frozenSessionsByHash.containsKey(session.getHashId())) {
			throw new IllegalStateException(
					"Unexpected: Session is already managed by ReconnectionManager. " + session.toString());
		}
		if (session.getReconnectionSeconds() <= 0) {
			throw new IllegalStateException("Unexpected: Session cannot be frozen. " + session.toString());
		}

		this.frozenSessionsByHash.put(session.getHashId(), session);
		this.logger.debug("Session added in ReconnectionManager: " + session + ", ReconnTime: "
				+ session.getReconnectionSeconds() + "s");
	}

	private void removeSession(ISession session) {
		this.frozenSessionsByHash.remove(session.getHashId());
		this.logger.debug("Session removed from ReconnectionManager: " + session);
	}

	private void dispatchSessionReconnectionSuccessEvent(ISession session) {
		Event event = new Event("sessionReconnectionSuccess");
		event.setParameter("session", session);
		this.engine.dispatchEvent(event);
	}

	private void dispatchSessionReconnectionFailureEvent(ISession incomingSession) {
		Event event = new Event("sessionReconnectionFailure");
		event.setParameter("session", incomingSession);
		this.engine.dispatchEvent(event);
	}

	private void applySessionCleaning() {
		if (this.frozenSessionsByHash.size() > 0) {
			for (Iterator<ISession> iter = this.frozenSessionsByHash.values().iterator(); iter.hasNext();) {
				ISession session = iter.next();

				if (!session.isReconnectionTimeExpired())
					continue;
				iter.remove();
				this.logger.debug("Removing expired reconnectable Session: " + session);

				session.setReconnectionSeconds(0);
				try {
					this.sessionManager.onSocketDisconnected(session);
				} catch (IOException e) {
					this.logger.warn("I/O Error while closing session: " + session);
				}
			}
		}
	}

	private final class ReconnectionSessionCleaner implements ITaskHandler {
		private ReconnectionSessionCleaner() {
		}

		public void doTask(Task task) throws Exception {
			DefaultReconnectionManager.this.applySessionCleaning();
		}
	}
}