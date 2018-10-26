package com.net.business.extensions.core;

import com.net.server.GameServer;
import com.net.server.api.IGameApi;
import com.net.server.core.ISFSEvent;
import com.net.server.core.ISFSEventListener;
import com.net.server.core.SFSEventType;
import com.net.server.data.IGameObject;
import com.net.server.entities.User;
import com.net.server.exceptions.SFSRuntimeException;
import io.netty.util.concurrent.ScheduledFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class BaseGameExtension implements IGameExtension, ISFSEventListener {
	private String name;
	private volatile boolean active;
	private final GameServer server;
	private final Logger logger;
	protected final IGameApi sfsApi;

	public BaseGameExtension() {
		this.logger = LoggerFactory.getLogger(BaseGameExtension.class);
		
		this.active = true;

		this.server = GameServer.getInstance();
		this.sfsApi = this.server.getAPIManager().getSFSApi();
	}
	
	public ScheduledFuture<?> addTask(int reqId,Runnable task,int time){
		ScheduledFuture<?> future=this.server.getSystemThreadPool().next(reqId).schedule(task, time, TimeUnit.SECONDS);
		return future;
	}
	public ScheduledFuture<?> addTask(int reqId,Runnable task,int delay,int reply){
		ScheduledFuture<?> future=this.server.getSystemThreadPool().next(reqId).scheduleWithFixedDelay(task,delay,reply,TimeUnit.SECONDS);
		return future;
	}
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		if (this.name != null) {
			throw new SFSRuntimeException("Cannot redefine name of extension: " + toString());
		}
		this.name = name;

	}

	public IGameApi getApi() {
		return this.sfsApi;
	}

	public void handleServerEvent(ISFSEvent event) throws Exception {
	}

	public void addEventListener(SFSEventType eventType, ISFSEventListener listener) {
		this.server.getExtensionManager().addExtensionEventListener(eventType, listener);
	}

	public void removeEventListener(SFSEventType eventType, ISFSEventListener listener) {
		this.server.getExtensionManager().removeEventListener(eventType, listener);
	}

	public boolean isActive() {
		return this.active;
	}

	public void setActive(boolean flag) {
		this.active = flag;
	}

	public void send(Object cmdName, IGameObject params, List<User> recipients) {
		send(cmdName, params, recipients, false);
	}

	public void send(Object cmdName, IGameObject params, User recipient) {
		send(cmdName, params, recipient, false);
	}

	public void send(Object cmdName, IGameObject params, List<User> recipients, boolean useUDP) {
		if (useUDP) {
			params.removeElement("$FS_REQUEST_UDP_TIMESTAMP");
		}
		this.sfsApi.sendExtensionResponse(cmdName, params, recipients, null, useUDP);
	}

	public void send(Object cmdName, IGameObject params, User recipient, boolean useUDP) {
		if (useUDP) {
			params.removeElement("$FS_REQUEST_UDP_TIMESTAMP");
		}
		this.sfsApi.sendExtensionResponse(cmdName, params, recipient, null, useUDP);
	}

	public Logger getLogger() {
		return this.logger;
	}

	public void trace(Object[] args) {
		trace(ExtensionLogLevel.INFO, args);
	}

	public void trace(ExtensionLogLevel level, Object[] args) {
		String traceMsg = getTraceMessage(args);

		if (level == ExtensionLogLevel.DEBUG) {
			this.logger.debug(traceMsg);
		} else if (level == ExtensionLogLevel.INFO) {
			this.logger.info(traceMsg);
		} else if (level == ExtensionLogLevel.WARN) {
			this.logger.warn(traceMsg);
		} else if (level == ExtensionLogLevel.ERROR) {
			this.logger.error(traceMsg);
		}
	}

	private String getTraceMessage(Object[] args) {
		StringBuilder traceMsg = new StringBuilder().append("{").append(this.name).append("}: ");

		for (Object o : args) {
			traceMsg.append(o.toString()).append(" ");
		}
		return traceMsg.toString();
	}

	protected void removeEventsForListener(SFSEventType type, ISFSEventListener listener) {
		this.server.getExtensionManager().removeEventListener(type, listener);
	}

}