package com.net.server.api;

import com.net.engine.service.IService;
import com.net.server.GameServer;

public class APIManager implements IService {
	private final String serviceName = "APIManager";
	private GameServer server;
	private IGameApi sfsApi;

	public void init(Object o) {
		this.server = GameServer.getInstance();
		this.sfsApi = new GameApi(this.server);
	}

	public IGameApi getSFSApi() {
		return this.sfsApi;
	}

	public void destroy(Object arg0) {
	}

	public String getName() {
		return serviceName;
	}

	public void handleMessage(Object msg) {
		throw new UnsupportedOperationException("Not supported");
	}

	public void setName(String arg0) {
		throw new UnsupportedOperationException("Not supported");
	}
}