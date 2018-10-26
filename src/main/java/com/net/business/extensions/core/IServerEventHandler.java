package com.net.business.extensions.core;

import com.net.server.core.ISFSEvent;

public abstract interface IServerEventHandler {
	public abstract void handleServerEvent(ISFSEvent event) throws Exception;

	public abstract void setParentExtension(GameExtension gameExtension);

	public abstract GameExtension getParentExtension();
}