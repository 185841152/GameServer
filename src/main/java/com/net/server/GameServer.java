package com.net.server;

import com.net.business.spring.SpringCtxInit;
import com.net.engine.config.ControllerConfig;
import com.net.engine.config.EngineConfiguration;
import com.net.engine.config.SocketConfig;
import com.net.engine.core.NetEngine;
import com.net.engine.data.BindableSocket;
import com.net.engine.data.TransportType;
import com.net.engine.events.IEvent;
import com.net.engine.events.IEventListener;
import com.net.engine.sessions.ISession;
import com.net.engine.sessions.ISessionManager;
import com.net.engine.websocket.WebSocketConfig;
import com.net.server.api.APIManager;
import com.net.server.config.*;
import com.net.server.core.ISFSEventManager;
import com.net.server.core.SFSEvent;
import com.net.server.core.SFSEventManager;
import com.net.server.core.SFSEventType;
import com.net.server.entities.managers.*;
import com.net.server.exceptions.ExceptionMessageComposer;
import com.net.server.exceptions.GameException;
import com.net.server.exceptions.SFSRuntimeException;
import com.net.server.util.SFSRestart;
import com.net.server.util.ServerUptime;
import com.net.server.util.TaskScheduler;
import com.net.server.util.executor.ExecutorConfig;
import com.net.server.util.executor.NettyDefaultEventExecutorGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.net.BindException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public final class GameServer {
	private final String version = "1.0.0";
	private static GameServer _instance = null;
	private static AtomicInteger restartCounter = new AtomicInteger(0);
	private final ServiceBootStrapper serviceBootStrapper;
	private final NetEngine netEngine;
	private final Logger log;
	private APIManager apiManager;
	private volatile boolean initialized = false;
	private volatile boolean started = false;
	private volatile long serverStartTime;
	private volatile boolean isRebooting = false;
	private volatile boolean isHalting = false;
	private IConfigurator sfsConfigurator;
	private IEventListener networkEvtListener;
	private TaskScheduler taskScheduler;
	private NettyDefaultEventExecutorGroup sysmtemWorkerPool;
	private boolean clustered = false;
	private final ISFSEventManager eventManager;
	private IUserManager userManager;
	private IZoneManager zoneManager;
	private ExtensionManager extensionManager;

	public static GameServer getInstance() {
		if (_instance == null) {
			_instance = new GameServer();
		}
		return _instance;
	}

	private GameServer() {
		this.serviceBootStrapper = ServiceBootStrapper.getInstance();
		this.netEngine = NetEngine.getInstance();
		this.log = LoggerFactory.getLogger(getClass());

		this.extensionManager = new ExtensionManager();
		this.networkEvtListener = new NetworkEvtListener();

		this.eventManager = new SFSEventManager();

		if (this.userManager == null) {
			this.userManager = new GameUserManager();
		}
		this.zoneManager = new GameZoneManager();

		this.taskScheduler = new TaskScheduler(1);

		this.clustered = "true".equalsIgnoreCase(System.getProperty("sfs2x.grid.active"));
	}

	public String getVersion() {
		return version;
	}

	public void start() {
		try {
			if (!this.initialized) {
				initialize();
			}

			this.sfsConfigurator.loadConfiguration();

			initSystemWorkers();

			configureServer();

			configureBitSwarm();

			zoneManager.initializeZones();

			this.netEngine.start("NetWorkServer 1X");
			
		} catch (FileNotFoundException e) {
			ExceptionMessageComposer msg = new ExceptionMessageComposer(e);
			msg.setDescription("服务器启动失败");
			msg.setPossibleCauses("检查服务器配置文件");

			this.log.error(msg.toString());
		} catch (BindException e) {
			ExceptionMessageComposer msg = new ExceptionMessageComposer(e);
			msg.setDescription("绑定TCP端口失败");
			msg.setPossibleCauses("请检查端口是否被占用");
			msg.addInfo("服务器启动失败");
			this.log.error(msg.toString());
		} catch (GameException e) {
			ExceptionMessageComposer msg = new ExceptionMessageComposer(e);
			msg.setDescription("服务器错误");

			this.log.error(msg.toString());
		} catch (Exception e) {
			ExceptionMessageComposer msg = new ExceptionMessageComposer(e);
			msg.setDescription("服务器启动失败");
			msg.addInfo(e.getMessage());

			this.log.error(msg.toString());
		}
	}

	public boolean isGrid() {
		return this.clustered;
	}

	public int getRestartCount() {
		return restartCounter.get();
	}

	public synchronized void restart() {
		if (this.isRebooting) {
			return;
		}
		this.isRebooting = true;

		this.log.warn("*** SERVER RESTARTING ***");
		try {
			this.netEngine.shutDownSequence();
			this.started = false;

			Thread restarter = new SFSRestart();
			restarter.start();
		} catch (Exception e) {
			this.log.error("Restart Failure: " + e);
		}
	}

	public void halt() {
		if (this.isHalting) {
			return;
		}
		this.isHalting = true;

		this.log.warn("*** SERVER HALTING ***");
		try {
			Thread stopper = new Thread(new Runnable() {
				int countDown = 3;

				public void run() {
					while (this.countDown > 0) {
						GameServer.this.log.warn("Server Halt in " + this.countDown-- + " seconds...");
						try {
							Thread.sleep(1000L);
						} catch (InterruptedException localInterruptedException) {
						}
					}
					System.exit(0);
				}
			});
			stopper.start();
		} catch (Exception e) {
			this.log.error("Halt Failure: " + e);
		}
	}

	public boolean isStarted() {
		return this.started;
	}

	public boolean isProcessControlAllowed() {
		String osName = System.getProperty("os.name");

		return (osName.toLowerCase().indexOf("linux") != -1) || (osName.toLowerCase().indexOf("mac os x") != -1)
				|| (osName.toLowerCase().indexOf("windows") != -1);
	}

	private void initialize() {
		if (this.initialized) {
			throw new IllegalStateException("已经初始化了!");
		}

		this.sfsConfigurator = ((IConfigurator) this.serviceBootStrapper.load(SFSService.Configurator));

		this.log.info("Boot sequence starts...");

		// 启动spring容器
		initSpringContainer();

		this.apiManager = new APIManager();
		this.apiManager.init(null);
		

		this.netEngine.addEventListener("serverStarted", this.networkEvtListener);
		this.netEngine.addEventListener("sessionAdded", this.networkEvtListener);
		this.netEngine.addEventListener("sessionLost", this.networkEvtListener);
		this.netEngine.addEventListener("sessionIdle", this.networkEvtListener);
		this.netEngine.addEventListener("sessionIdleCheckComplete", this.networkEvtListener);

		this.netEngine.addEventListener("packetDropped", this.networkEvtListener);

		this.netEngine.addEventListener("sessionReconnectionTry", this.networkEvtListener);
		this.netEngine.addEventListener("sessionReconnectionSuccess", this.networkEvtListener);
		this.netEngine.addEventListener("sessionReconnectionFailure", this.networkEvtListener);

		this.initialized = true;
	}

	public void initSpringContainer() {
		SpringCtxInit.instance();
		this.log.info("init spring container success. ");
	}

	private void initSystemWorkers() {
		ExecutorConfig cfg = this.sfsConfigurator.getServerSettings().systemThreadPoolSettings;
		cfg.name = "Sys";

		this.sysmtemWorkerPool = new NettyDefaultEventExecutorGroup(cfg.coreThreads);
	}

	public NettyDefaultEventExecutorGroup getSystemThreadPool() {
		return this.sysmtemWorkerPool;
	}

	private void configureServer() {
		ServerSettings settings = this.sfsConfigurator.getServerSettings();

		this.taskScheduler.resizeThreadPool(settings.schedulerThreadPoolSize);

		this.eventManager.init(null);

		this.zoneManager.init(null);
		
		this.extensionManager.init();

	}

	private void configureBitSwarm() {
		EngineConfiguration engineConfiguration = new EngineConfiguration();

		CoreSettings coreSettings = this.sfsConfigurator.getCoreSettings();
		ServerSettings sfsSettings = this.sfsConfigurator.getServerSettings();

		for (ServerSettings.SocketAddress addr : sfsSettings.socketAddresses) {
			engineConfiguration
					.addBindableAddress(new SocketConfig(addr.address, addr.port, TransportType.fromName(addr.type)));
		}

		engineConfiguration.setClustered(sfsSettings.isCluster);
		for (ServerSettings.ClusterAddress addr : sfsSettings.clusterAddresses) {
			engineConfiguration.addClusterAddress(addr);
		}

		if (sfsSettings.enableSmasherController) {
			engineConfiguration
					.addController(new ControllerConfig("com.smartfoxserver.v2.controllers.v290.SmasherReqController",
							DefaultConstants.CORE_SMASHER_CONTROLLER_ID, 1, 1000));
		}

		engineConfiguration.setDefaultMaxSessionIdleTime(sfsSettings.sessionMaxIdleTime);
		try {
			engineConfiguration.setDefaultMaxLoggedInSessionIdleTime(sfsSettings.userMaxIdleTime);
		} catch (IllegalArgumentException err) {
			engineConfiguration.setDefaultMaxLoggedInSessionIdleTime(sfsSettings.sessionMaxIdleTime + 60);

			ExceptionMessageComposer msg = new ExceptionMessageComposer(err);
			msg.setDescription("Make sure that userMaxIdleTime > socketIdleTime");
			msg.addInfo("The problem was temporarily fixed by setting userMaxIdleTime as: "
					+ engineConfiguration.getDefaultMaxLoggedInSessionIdleTime());
			msg.addInfo("Please review your server.xml file and fix the problem.");

			this.log.warn(msg.toString());

			engineConfiguration.setDefaultMaxLoggedInSessionIdleTime(sfsSettings.sessionMaxIdleTime + 60);
		}

		String protocolType = "Protocol Type is: ";

		if (sfsSettings.useBinaryProtocol) {
			engineConfiguration.setIoHandlerClass("com.smartfoxserver.v2.protocol.SFSIoHandler");
			protocolType = protocolType + "BINARY";
		} else {
			engineConfiguration.setIoHandlerClass("com.smartfoxserver.v2.protocol.SFSTxtIoHandler");
			protocolType = protocolType + "JSON";
		}

		this.log.info(protocolType);

		engineConfiguration.setMaxIncomingRequestSize(coreSettings.maxIncomingRequestSize);
		engineConfiguration.setSessionPacketQueueMaxSize(coreSettings.sessionPacketQueueSize);
		engineConfiguration.setNagleAlgorithm(!coreSettings.tcpNoDelay);
		engineConfiguration.setPacketDebug(coreSettings.packetDebug);
		engineConfiguration.setLagDebug(coreSettings.lagDebug);

		engineConfiguration.setAcceptorThreadPoolSize(coreSettings.socketAcceptorThreadPoolSize);
		engineConfiguration.setReaderThreadPoolSize(coreSettings.socketReaderThreadPoolSize);
		engineConfiguration.setWriterThreadPoolSize(coreSettings.socketWriterThreadPoolSize);

		engineConfiguration.setMaxConnectionsFromSameIp(99999);

		WebSocketConfig wsc = new WebSocketConfig();

		if (sfsSettings.webSocket != null) {
			wsc.setActive(sfsSettings.webSocket.isActive);
			wsc.setHost(sfsSettings.webSocket.bindAddress);
			wsc.setPort(sfsSettings.webSocket.tcpPort);
			wsc.setSslPort(sfsSettings.webSocket.sslPort);
			wsc.setSSL(sfsSettings.webSocket.isSSL);
			wsc.setKeyStoreFile(sfsSettings.webSocket.keyStoreFile);
			wsc.setKeyStorePassword(sfsSettings.webSocket.keyStorePassword);
		}

		engineConfiguration.setWebSocketEngineConfig(wsc);

		this.netEngine.setConfiguration(engineConfiguration);
	}

	private final class EventDelegate implements Runnable {
		private final IEvent event;

		public EventDelegate(IEvent event) {
			this.event = event;
		}

		public void run() {
			try {
				String evtName = this.event.getName();

				if (evtName.equals("serverStarted")) {
					GameServer.this.onSocketEngineStart();
				} else if (evtName.equals("sessionLost")) {
					ISession session = (ISession) this.event.getParameter("session");

					if (session == null) {
						throw new SFSRuntimeException("UNEXPECTED: Session was lost, but session object is NULL!");
					}
					GameServer.this.onSessionClosed(session);
				} else if ((evtName.equals("sessionIdleCheckComplete"))) {

//				} else if (evtName.equals("sessionIdle")) {
//					GameServer.this.onSessionIdle((ISession) this.event.getParameter("session"));
//				} else if (evtName.equals("sessionReconnectionTry")) {
//					GameServer.this.onSessionReconnectionTry((ISession) this.event.getParameter("session"));
//				} else if (evtName.equals("sessionReconnectionSuccess")) {
//					GameServer.this.onSessionReconnectionSuccess((ISession) this.event.getParameter("session"));
				} else if (evtName.equals("sessionReconnectionFailure")) {
					GameServer.this.onSessionReconnectionFailure((ISession) this.event.getParameter("session"));
				}

			} catch (Throwable t) {
				GameServer.this.log.warn(t.getMessage());
			}
		}
	}

	private final class NetworkEvtListener implements IEventListener {
		private NetworkEvtListener() {
		}

		public void handleEvent(IEvent event) {
			GameServer.this.sysmtemWorkerPool.execute(new GameServer.EventDelegate(event));
		}
	}

	private void onSocketEngineStart() {
		for (String blockedIp : this.sfsConfigurator.getServerSettings().ipFilter.addressBlackList) {
			this.netEngine.getSocketAcceptor().getConnectionFilter().addBannedAddress(blockedIp);
		}

		for (String allowedIp : this.sfsConfigurator.getServerSettings().ipFilter.addressWhiteList) {
			this.netEngine.getSocketAcceptor().getConnectionFilter().addWhiteListAddress(allowedIp);
		}

		this.netEngine.getSocketAcceptor().getConnectionFilter()
				.setMaxConnectionsPerIp(this.sfsConfigurator.getServerSettings().ipFilter.maxConnectionsPerAddress);

		List<BindableSocket> sockets = this.netEngine.getSocketAcceptor().getBoundSockets();
		String message = "Listening Sockets: ";

		for (BindableSocket socket : sockets) {
			message = message + socket.toString() + " ";
		}
		if ((this.sfsConfigurator.getServerSettings().webSocket != null)
				&& (this.sfsConfigurator.getServerSettings().webSocket.isActive)) {
			message = message + "{ " + this.sfsConfigurator.getServerSettings().webSocket.bindAddress + ":"
					+ (this.sfsConfigurator.getServerSettings().webSocket.isSSL
							? this.sfsConfigurator.getServerSettings().webSocket.sslPort + " (SSL)"
							: Integer.valueOf(this.sfsConfigurator.getServerSettings().webSocket.tcpPort))
					+ " (WebSocket) }";
		}

		this.log.info(message);

		this.log.info("SmartFoxServer 2X (" + version + ") READY!");
		System.out.println("************************************************************");
		System.out.println(" .--,       .--,");
		System.out.println("( (  \\.---./  ) )");
		System.out.println(" '.__/o   o\\__.'");
		System.out.println("    {=  ^  =}");
		System.out.println("     >  -  <");
		System.out.println("    /       \\");
		System.out.println("   //       \\\\");
		System.out.println("  //|   .   |\\\\");
		System.out.println("  \"'\\       /'\"_.-~^`'-.");
		System.out.println("     \\  _  /--'         `");
		System.out.println("   ___)( )(___");
		System.out.println("  (((__) (__)))    高山仰止,景行行止.虽不能至,心向往之。");
		System.out.println("************************************************************");
		this.serverStartTime = System.currentTimeMillis();
		this.started = true;

		this.eventManager.dispatchEvent(new SFSEvent(SFSEventType.SERVER_READY));

		if (this.netEngine.getConfiguration().isPacketDebug()) {
			this.log.info("<< PACKET DEBUGGER ACTIVE >>");
		}

	}

	private void onSessionClosed(ISession session) {
//		this.apiManager.getSFSApi().disconnect(session);
	}

//	private void onSessionIdle(ISession idleSession) {
//		User user = getUserManager().getUserBySession(idleSession);
//
//		if (user == null) {
//			throw new SFSRuntimeException(
//					"IdleSession event ignored, cannot find any User for Session: " + idleSession);
//		}
//
//		this.apiManager.getSFSApi().disconnectUser(user, ClientDisconnectionReason.IDLE);
//	}
//
//	private void onSessionReconnectionTry(ISession session) {
//		User user = getUserManager().getUserBySession(session);
//
//		if (user == null) {
//			throw new SFSRuntimeException("-Unexpected- Cannot find any User for Session: " + session);
//		}
//
//		Map<ISFSEventParam, Object> evtParams = new HashMap<ISFSEventParam, Object>();
//		evtParams.put(SFSEventParam.USER, user);
//
//		this.eventManager.dispatchEvent(new SFSEvent(SFSEventType.USER_RECONNECTION_TRY, evtParams));
//	}
//
//	private void onSessionReconnectionSuccess(ISession session) {
//		User user = getUserManager().getUserBySession(session);
//
//		if (user == null) {
//			throw new SFSRuntimeException("-Unexpected- Cannot find any User for Session: " + session);
//		}
//
//		Map<ISFSEventParam, Object> evtParams = new HashMap<ISFSEventParam, Object>();
//		evtParams.put(SFSEventParam.USER, user);
//
//		this.eventManager.dispatchEvent(new SFSEvent(SFSEventType.USER_RECONNECTION_SUCCESS, evtParams));
//	}

	private void onSessionReconnectionFailure(ISession incomingSession) {
//		this.apiManager.getSFSApi().getResponseAPI().notifyReconnectionFailure(incomingSession);
	}

	public TaskScheduler getTaskScheduler() {
		return this.taskScheduler;
	}

	public ISFSEventManager getEventManager() {
		return this.eventManager;
	}

	public IUserManager getUserManager() {
		return this.userManager;
	}

	public IZoneManager getZoneManager() {
		return zoneManager;
	}

	public ISessionManager getSessionManager() {
		return this.netEngine.getSessionManager();
	}

	public IConfigurator getConfigurator() {
		return this.sfsConfigurator;
	}

	public int getMinClientApiVersion() {
		return 60;
	}

	public APIManager getAPIManager() {
		return this.apiManager;
	}

	public ServerUptime getUptime() {
		if (this.serverStartTime == 0L) {
			throw new IllegalStateException("Server not ready yet, cannot provide uptime!");
		}
		return new ServerUptime(System.currentTimeMillis() - this.serverStartTime);
	}

	public ExtensionManager getExtensionManager() {
		return extensionManager;
	}
}