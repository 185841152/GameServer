package com.net.server.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.net.business.extensions.core.IGameExtension;
import com.net.engine.controllers.SimpleController;
import com.net.engine.exceptions.RequestQueueFullException;
import com.net.engine.io.IRequest;
import com.net.engine.io.IResponse;
import com.net.engine.io.Response;
import com.net.engine.util.Logging;
import com.net.server.GameServer;
import com.net.server.controllers.system.Login;
import com.net.server.data.GameObject;
import com.net.server.data.IGameObject;
import com.net.server.entities.User;
import com.net.server.entities.managers.ExtensionManager;
import com.net.server.exceptions.ErrorCode;
import com.net.server.exceptions.ExceptionMessageComposer;
import com.net.server.exceptions.SFSExtensionException;
import com.net.server.util.executor.NettyDefaultEventExecutorGroup;

public class ExtensionReqController extends SimpleController {
	public static final String KEY_EXT_CMD = "c";
	public static final String KEY_EXT_PARAMS = "p";
	public static final String KEY_ROOMID = "r";
	private ExtensionManager extensionManager;
	private final Logger logger;
	private final GameServer server;
	private NettyDefaultEventExecutorGroup threadPool;
	private int qSize;

	public ExtensionReqController() {
		this.logger = LoggerFactory.getLogger(getClass());
		this.server = GameServer.getInstance();
		extensionManager = server.getExtensionManager();
	}

	public void init(Object o) {
		super.init(o);
		this.threadPool = this.server.getEventManager().getThreadPool();
	}

	public void enqueueRequest(IRequest request) throws RequestQueueFullException {
		try {
			IGameObject reqObj = (IGameObject) request.getContent();
			
			Integer sendUserId=reqObj.getInt(Login.KEY_USERID);
			if (sendUserId==null || sendUserId<=0) {
				throw new SFSExtensionException("请求失败，用户ID不能为空");
			}
			User sender = this.server.getUserManager().getUserById(sendUserId);
			if (sender != null) {
				int reqId=sendUserId;
				if (sender.getLastJoinedRoom()!=null) {
					reqId=sender.getLastJoinedRoom().getId();
				}
				this.threadPool.next(reqId).execute(new RequestRunner(this, request));
			}
		} catch (Exception t) {
			t.printStackTrace();
			throw new RequestQueueFullException();
		}

	}

	protected void processRequest(IRequest request) throws Exception {
		if (this.logger.isDebugEnabled()) {
			this.logger.debug(request.toString());
		}
		IGameObject reqObj = (IGameObject) request.getContent();
		
		Integer sendUserId=reqObj.getInt(Login.KEY_USERID);
		if (sendUserId==null || sendUserId<=0) {
			throw new SFSExtensionException("Extension Request refused. user Id mission");
		}
		User sender = this.server.getUserManager().getUserById(sendUserId);
		if (sender == null) {
			throw new SFSExtensionException("Extension Request refused. Sender is not a User: " + request.getSender());
		}
		if (this.logger.isDebugEnabled() && reqObj != null) {
			this.logger.debug(reqObj.getDump());
		}

		Object cmd = request.getId();

		if (cmd == null) {
			throw new SFSExtensionException("Extension Request refused. Missing CMD. " + sender);
		}
		IGameExtension extension = this.extensionManager.getExtension();//getZoneExtension(zone);

		if (extension == null) {
			throw new SFSExtensionException("未发现扩展");//String.format("未发现扩展",zone.getName())
		}
		try{
			extension.handleClientRequest(cmd, sender, reqObj);
		}catch (Exception e){
			ExceptionMessageComposer composer = new ExceptionMessageComposer(e);
			composer.setDescription("Error while handling client request in extension: " + extension.toString());
			composer.addInfo("Extension Cmd: " + cmd);
			this.logger.error(composer.toString());
			//发送异常事件
			IGameObject error=GameObject.newInstance();
			error.putShort("ec", ErrorCode.COMMON_SYSTEM_ERROR.getId());
			IResponse response = new Response();
			response.setId(cmd);
			response.setContent(error);
			response.setRecipients(request.getSender());
			response.setUserId(sendUserId);

			response.write();
		}
	}

	@Override
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
		return this.threadPool.executorCount();
	}

	public void setThreadPoolSize(int size) {
	}

	public void handleMessage(Object message) {
	}
	
	public final class RequestRunner implements Runnable {
		private final ExtensionReqController controller;
		private final IRequest request;

		public RequestRunner(ExtensionReqController controller, IRequest request) {
			this.controller = controller;
			this.request = request;
		}

		public void run() {
			try {
				controller.processRequest(request);
			} catch (Exception e) {
				Logging.logStackTrace(ExtensionReqController.this.logger, e);
			}
		}
	}

}