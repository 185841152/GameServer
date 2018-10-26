package com.net.server.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.net.engine.io.IRequest;
import com.net.server.GameServer;
import com.net.server.api.IGameApi;
import com.net.server.entities.User;

public abstract class BaseControllerCommand implements IControllerCommand {
	public static final String KEY_ERROR_CODE = "ec";
	public static final String KEY_ERROR_PARAMS = "ep";
	protected final Logger logger;
	protected final GameServer server;
	protected final IGameApi api;
	private short id;
	private final SystemRequest requestType;

	public BaseControllerCommand(SystemRequest request) {
		this.logger = LoggerFactory.getLogger(getClass());
		this.server = GameServer.getInstance();
		this.api = this.server.getAPIManager().getSFSApi();
		this.id = ((Short) request.getId()).shortValue();
		this.requestType = request;
	}

	public Object preProcess(IRequest request) throws Exception {
		return null;
	}

	public short getId() {
		return this.id;
	}

	public SystemRequest getRequestType() {
		return this.requestType;
	}

	protected void applyZoneFilterChain(User user, IRequest request) {
	}
}