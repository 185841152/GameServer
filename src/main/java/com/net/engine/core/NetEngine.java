package com.net.engine.core;

import com.net.engine.config.EngineConfiguration;
import com.net.engine.config.SocketConfig;
import com.net.engine.controllers.DefaultControllerManager;
import com.net.engine.controllers.IController;
import com.net.engine.controllers.IControllerManager;
import com.net.engine.core.security.DefaultSecurityManager;
import com.net.engine.core.security.ISecurityManager;
import com.net.engine.events.Event;
import com.net.engine.events.IEventListener;
import com.net.engine.exceptions.BootSequenceException;
import com.net.engine.io.IResponse;
import com.net.engine.io.Response;
import com.net.engine.service.BaseCoreService;
import com.net.engine.service.IService;
import com.net.engine.sessions.DefaultSessionManager;
import com.net.engine.sessions.ISession;
import com.net.engine.sessions.ISessionManager;
import com.net.engine.sessions.SessionType;
import com.net.engine.util.scheduling.ITaskHandler;
import com.net.engine.util.scheduling.Scheduler;
import com.net.engine.websocket.WebSocketService;
import com.net.server.config.DefaultConstants;
import com.net.server.controllers.ExtensionReqController;
import com.net.server.controllers.SystemReqController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 引擎核心类
 * 
 * @author sunjian
 *
 */
public final class NetEngine extends BaseCoreService {
	public static final String version = "1.0.0";

	private Logger logger;
	private Logger bootLogger;

	private static NetEngine __engine__;

	private ISocketAcceptor socketAcceptor;
	private ISocketWriter socketWriter;
	private ISocketReader socketReader;
	private WebSocketService webSocketService;

	private Scheduler scheduler;
	private EngineConfiguration configuration;
	private ISessionManager sessionManager;
	private IControllerManager controllerManager;
	private ISecurityManager securityManager;

	private volatile boolean running = false;
	private volatile boolean restarting = false;
	private volatile boolean inited = false;

	private Map<String, IService> coreServicesByName;
	private Map<IService, Object> configByService;

	private IEventListener eventHandler;
	private EngineDelayedTaskHandler engineDelayedTaskHandler;
	private volatile int restartCount = 0;

	/**
	 * 获取引擎实例
	 * 
	 * @return
	 */
	public static NetEngine getInstance() {
		if (__engine__ == null) {
			__engine__ = new NetEngine();
		}
		return __engine__;
	}

	private NetEngine() {
		setName("NetEngine 1.0.0");
	}

	/**
	 * 初始化引擎配置信息
	 */
	private void initializeServerEngine() {
		this.logger = LoggerFactory.getLogger(NetEngine.class);
		this.bootLogger = LoggerFactory.getLogger(NetEngine.class);
		this.inited = true;
	}

	/**
	 * 启动入口
	 * 
	 * @throws Exception
	 */
	public void start() throws Exception {
		start(null);
	}

	/**
	 * 启动引擎
	 * 
	 * @param extraLogMessage
	 * @throws Exception
	 */
	public void start(String extraLogMessage) throws Exception {
		if (!this.inited) {
			initializeServerEngine();
		}
		if (extraLogMessage != null) {
			this.bootLogger.info(extraLogMessage);
		}

		this.engineDelayedTaskHandler = new EngineDelayedTaskHandler();
		this.coreServicesByName = new ConcurrentHashMap<String, IService>();
		this.configByService = new HashMap<IService, Object>();

		bootSequence();

		// 发送服务器启动事件
		Event engineStartedEvent = new Event("serverStarted");
		dispatchEvent(engineStartedEvent);

		this.running = true;

	}

	/**
	 * 重启系统
	 */
	public void restart() {
		Thread runningThread = Thread.currentThread();
		if (!this.securityManager.isEngineThread(runningThread)) {
			this.logger.error(String.format("This thread is not allowed to perform a restart: %s (%s) ",
					new Object[] { runningThread.getName(), runningThread.getThreadGroup().getName() }));
			return;
		}

		this.restartCount += 1;

		if ((this.restarting) || (!this.running)) {
			return;
		}
		this.bootLogger.info("Restart Sequence inited...");

		this.restarting = true;
		this.running = false;

		Thread restarter = new Thread(new Runnable() {
			public void run() {
				NetEngine.this.halt();
				NetEngine.this.bootLogger.info("Restart Sequence complete!");
			}
		}, "--== Restarter ==--");

		restarter.start();
	}

	/**
	 * 系统停止
	 */
	public void halt() {
		try {
			boolean needRestart = this.restarting;
			this.bootLogger.info("Halting Server Engine...");

			((BaseCoreService) this.socketAcceptor).removeEventListener("sessionAdded", this.eventHandler);

			shutDownSequence();

			this.socketAcceptor = null;
			this.socketWriter = null;
			this.eventHandler = null;
			this.engineDelayedTaskHandler = null;
			this.coreServicesByName = null;

			this.restarting = false;
			this.running = false;

			this.bootLogger.info("ShutDown Sequence Complete... ");

			if (needRestart) {
				System.gc();

				Thread.sleep(4000L);

				start("Restarting Server Engine...");
			}

		} catch (Throwable problem) {
			this.bootLogger.warn("Error while shutting down the server: " + problem.getMessage());
		}
	}

	/**
	 * 获取重启次数
	 * 
	 * @return
	 */
	public int getRestartCount() {
		return this.restartCount;
	}

	/**
	 * 启动引导
	 * 
	 * @throws BootSequenceException
	 * @throws Exception
	 */
	public void bootSequence() throws BootSequenceException, Exception {

		this.bootLogger.info("BitSwarmEngine version: 3.30.1 { " + Thread.currentThread().getName() + " }");

		// 启动核心服务
		startCoreServices();

		// 绑定TCP端口
		bindSockets(this.configuration.getBindableSockets());

		this.configByService.put(this.webSocketService, getConfiguration().getWebSocketEngineConfig());

		// 初始化核心服务
		for (IService service : this.coreServicesByName.values()) {
			if (service != null) {
				service.init(this.configByService.get(service));
			}
		}

		this.bootLogger.info("[[[ ===--- Boot sequence complete ---=== ]]]");
	}

	/**
	 * 停止
	 * 
	 * @throws Exception
	 */
	public void shutDownSequence() throws Exception {
		stopCoreServices();
	}

	/**
	 * 服务器往客户端写
	 * 
	 * @param response
	 */
	public void write(IResponse response) {
		try
		{
			ISession session = response.getRecipients();
			if (session.getType() == SessionType.WEBSOCKET) {
				IResponse webSocketResponse = Response.clone(response);
				writeToWebSocket(webSocketResponse);
			}else {

				writeToSocket(response);
			}
		}catch (Exception e){

		}
	}

	/**
	 * 通过TCP协议写
	 * 
	 * @param res
	 */
	private void writeToSocket(IResponse res) {
		this.socketWriter.onPacketWrite(res);
	}

	/**
	 * 通过websocket协议写
	 * @param res
	 */
	private void writeToWebSocket(IResponse res)
	{
		this.webSocketService.getProtocolCodec().onPacketWrite(res);
	}

	/**
	 * 启动核心服务
	 * 
	 * @throws Exception
	 */
	private void startCoreServices() throws Exception {
		// 安全控制器
		this.securityManager = new DefaultSecurityManager();
		// 任务调度器
		this.scheduler = new Scheduler(this.bootLogger);
		// session管理器
		this.sessionManager = DefaultSessionManager.getInstance();
		this.bootLogger.info("Session manager ready: " + this.sessionManager);
		// 系统业务控制器
		this.controllerManager = new DefaultControllerManager();
		// 配置业务控制器
		configureControllers();

		// socket接收器，负责socket的读
		this.socketAcceptor = new SocketAcceptor();
		this.socketAcceptor.getConnectionFilter().setMaxConnectionsPerIp(1024);
		// socket写入器，负责socket的写
		this.socketWriter = new SocketWriter(1);

		this.socketReader = new SocketReader();

		// webscoket服务
		this.webSocketService = new WebSocketService();
		this.coreServicesByName.put("webSocketEngine", this.webSocketService);

		this.securityManager.setName("securityManager");
		this.scheduler.setName("scheduler");
		this.sessionManager.setName("sessionManager");
		this.controllerManager.setName("controllerManager");

		((BaseCoreService) this.socketAcceptor).setName("socketAcceptor");
		((BaseCoreService) this.socketWriter).setName("socketWriter");

		this.coreServicesByName.put("scheduler", this.scheduler);
		this.coreServicesByName.put("socketWriter", (IService) this.socketWriter);
		this.coreServicesByName.put("socketAcceptor", (IService) this.socketAcceptor);
		this.coreServicesByName.put("sessionManager", this.sessionManager);
		this.coreServicesByName.put("controllerManager", this.controllerManager);
		this.coreServicesByName.put("securityManager", this.securityManager);
	}

	/**
	 * 停止核心服务
	 * 
	 * @throws Exception
	 */
	private void stopCoreServices() throws Exception {
		this.scheduler.destroy(null);

		Thread.sleep(2000L);

		this.controllerManager.destroy(null);
		this.sessionManager.destroy(null);
		this.securityManager.destroy(null);
		((IService) this.socketAcceptor).destroy(null);
		((IService) this.socketWriter).destroy(null);
	}


	/**
	 * 配置业务控制器
	 */
	private void configureControllers() {
		// 引擎核心控制器
		IController systemcontroller = new SystemReqController();
		systemcontroller.setId(DefaultConstants.CORE_SYSTEM_CONTROLLER_ID);
		systemcontroller.setThreadPoolSize(4);
		systemcontroller.setMaxQueueSize(20000);
		this.controllerManager.addController(systemcontroller.getId(), systemcontroller);

		IController extensionController = new ExtensionReqController();
		extensionController.setId(DefaultConstants.CORE_EXTENSIONS_CONTROLLER_ID);
		extensionController.setThreadPoolSize(4);
		extensionController.setMaxQueueSize(20000);
		this.controllerManager.addController(extensionController.getId(), extensionController);
	}

	/**
	 * 根据配置绑定协议端口
	 * 
	 * @param bindableSockets
	 */
	private void bindSockets(List<SocketConfig> bindableSockets) {
		for (SocketConfig socketCfg : bindableSockets) {
			try {
				this.socketAcceptor.bindSocket(socketCfg);
			} catch (IOException e) {
				this.bootLogger.warn("Was not able to bind socket: " + socketCfg);
			}
		}
	}

	/**
	 * 根据服务名称获取服务
	 * 
	 * @param serviceName
	 * @return
	 */
	public IService getServiceByName(String serviceName) {
		return (IService) this.coreServicesByName.get(serviceName);
	}

	public ISocketAcceptor getSocketAcceptor() {
		return this.socketAcceptor;
	}

	public void init(Object o) {
		throw new UnsupportedOperationException("This call is not supported in this class!");
	}

	public void destroy(Object o) {
		throw new UnsupportedOperationException("This call is not supported in this class!");
	}

	public Logger getLogger() {
		return this.logger;
	}

	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	public EngineConfiguration getConfiguration() {
		return this.configuration;
	}

	public void setConfiguration(EngineConfiguration configuration) {
		this.configuration = configuration;
	}

	public ITaskHandler getEngineDelayedTaskHandler() {
		return this.engineDelayedTaskHandler;
	}

	public IControllerManager getControllerManager() {
		return this.controllerManager;
	}

	public ISessionManager getSessionManager() {
		return this.sessionManager;
	}

	public ISocketWriter getSocketWriter() {
		return this.socketWriter;
	}

	public ISocketReader getSocketReader() {
		return socketReader;
	}

	public void setSocketReader(ISocketReader socketReader) {
		this.socketReader = socketReader;
	}

}