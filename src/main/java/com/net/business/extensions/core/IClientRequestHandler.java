package com.net.business.extensions.core;

import com.net.server.data.IGameObject;
import com.net.server.entities.User;

public abstract interface IClientRequestHandler {
	public abstract void handleClientRequest(User user, IGameObject params) throws Exception;

	public abstract void setParentExtension(GameExtension extension);

	public abstract GameExtension getParentExtension();
}