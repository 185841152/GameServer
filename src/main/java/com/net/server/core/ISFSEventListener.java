package com.net.server.core;

public abstract interface ISFSEventListener {
	public abstract void handleServerEvent(ISFSEvent paramISFSEvent) throws Exception;
}