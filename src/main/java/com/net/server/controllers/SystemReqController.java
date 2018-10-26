package com.net.server.controllers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.net.engine.controllers.SimpleController;
import com.net.engine.exceptions.RequestQueueFullException;
import com.net.engine.io.IRequest;
import com.net.engine.util.Logging;
import com.net.server.GameServer;
import com.net.server.exceptions.GameRuntimeException;
import com.net.server.util.executor.NettyDefaultEventExecutorGroup;

public class SystemReqController extends SimpleController {
	private static final Map<Object, String> commandMap = new HashMap<Object, String>();
	private static final String commandPackage = "com.net.server.controllers.system.";
	private int qSize;
	private final GameServer server;
	private final Logger logger;
	private Map<Object, IControllerCommand> commandCache;
	private boolean useCache = true;
	private final NettyDefaultEventExecutorGroup systemThreadPool;

	static {
		commandMap.put(SystemRequest.Handshake.getId(), commandPackage + "Handshake");
		commandMap.put(SystemRequest.Login.getId(), commandPackage + "Login");
		commandMap.put(SystemRequest.Logout.getId(), commandPackage + "Logout");
		commandMap.put(SystemRequest.JoinRoom.getId(), commandPackage + "JoinRoom");
		commandMap.put(SystemRequest.AutoJoin.getId(), commandPackage + "AutoJoin");
		commandMap.put(SystemRequest.CreateRoom.getId(), commandPackage + "CreateRoom");
		commandMap.put(SystemRequest.GenericMessage.getId(), commandPackage + "GenericMessage");
		commandMap.put(SystemRequest.ChangeRoomName.getId(), commandPackage + "ChangeRoomName");
		commandMap.put(SystemRequest.ChangeRoomPassword.getId(), commandPackage + "ChangeRoomPassword");
		commandMap.put(SystemRequest.ChangeRoomCapacity.getId(), commandPackage + "ChangeRoomCapacity");
		commandMap.put(SystemRequest.ObjectMessage.getId(), commandPackage + "SendObject");
		commandMap.put(SystemRequest.SetRoomVariables.getId(), commandPackage + "SetRoomVariables");
		commandMap.put(SystemRequest.SetUserVariables.getId(), commandPackage + "SetUserVariables");
		commandMap.put(SystemRequest.CallExtension.getId(), commandPackage + "CallExtension");
		commandMap.put(SystemRequest.LeaveRoom.getId(), commandPackage + "LeaveRoom");
		commandMap.put(SystemRequest.SubscribeRoomGroup.getId(), commandPackage + "SubscribeRoomGroup");
		commandMap.put(SystemRequest.UnsubscribeRoomGroup.getId(), commandPackage + "UnsubscribeRoomGroup");
		commandMap.put(SystemRequest.PlayerToSpectator.getId(), commandPackage + "PlayerToSpectator");
		commandMap.put(SystemRequest.SpectatorToPlayer.getId(), commandPackage + "SpectatorToPlayer");
		commandMap.put(SystemRequest.KickUser.getId(), commandPackage + "KickUser");
		commandMap.put(SystemRequest.BanUser.getId(), commandPackage + "BanUser");
		commandMap.put(SystemRequest.ManualDisconnection.getId(), commandPackage + "ManualDisconnection");
		commandMap.put(SystemRequest.FindRooms.getId(), commandPackage + "FindRooms");
		commandMap.put(SystemRequest.FindUsers.getId(), commandPackage + "FindUsers");
		commandMap.put(SystemRequest.PingPong.getId(), commandPackage + "PingPong");
		commandMap.put(SystemRequest.SetUserPosition.getId(), commandPackage + "SetUserPosition");

	}

	public SystemReqController() {
		this.server = GameServer.getInstance();
		this.logger = LoggerFactory.getLogger(getClass());
		this.systemThreadPool = this.server.getSystemThreadPool();
	}

	public void init(Object o) {
		super.init(o);
		this.commandCache = new ConcurrentHashMap<Object, IControllerCommand>();
	}

	public void enqueueRequest(IRequest request) throws RequestQueueFullException {
		try {
//			if (getQueueSize() > qSize) {
//				throw new RequestQueueFullException();
//			}
			this.systemThreadPool.execute(new RequestRunner(this, request));
		} catch (Exception t) {
			t.printStackTrace();
			throw new RequestQueueFullException();
		}
	}

	public final class RequestRunner implements Runnable {
		private final SystemReqController controller;
		private final IRequest request;

		public RequestRunner(SystemReqController controller, IRequest request) {
			this.controller = controller;
			this.request = request;
		}

		public void run() {
			try {
				controller.processRequest(request);
			} catch (Exception e) {
				Logging.logStackTrace(SystemReqController.this.logger, e);
			}
		}
	}

	protected void processRequest(IRequest request) throws Exception {
		if (this.logger.isDebugEnabled()) {
			this.logger.debug("{IN}: " + SystemRequest.fromId(request.getId()).toString());
		}
		IControllerCommand command = null;
		Object reqId = request.getId();

		if (this.useCache) {
			command = (IControllerCommand) this.commandCache.get(reqId);

			if (command == null) {
				command = getCommand(reqId);
			}
		} else {
			command = getCommand(reqId);
		}

		if (command != null) {
			if (command.validate(request)) {
				try {
					command.execute(request);
				} catch (GameRuntimeException re) {
					String msg = re.getMessage();

					if (msg != null)
						this.logger.warn(msg);
				}
			}
		}
	}

	private IControllerCommand getCommand(Object reqId) {
		IControllerCommand command = null;
		String className = (String) commandMap.get(reqId);

		if (className != null) {
			try {
				Class<?> clazz = Class.forName(className);
				command = (IControllerCommand) clazz.newInstance();
			} catch (Exception err) {
				this.logger.error("反射失败，类名: " + className + ", Error: " + err);
			}
		} else {
			this.logger.error("请求ID验证失败，未发现命令类: " + reqId);
		}

		return command;
	}

	public int getQueueSize() {
		return 0;
	}

	public int getMaxQueueSize() {
		return this.qSize;
	}

	public void setMaxQueueSize(int size) {
		this.qSize = size;
	}

	public int getThreadPoolSize() {
		return this.systemThreadPool.executorCount();
	}

	public void setThreadPoolSize(int size) {
	}

	public void handleMessage(Object message) {
	}
}