package com.net.business.extensions.core;

import java.util.List;

import com.net.server.data.IGameObject;
import com.net.server.entities.User;

public abstract class BaseClientRequestHandler implements IClientRequestHandler {

	private GameExtension parentExtension;

	public GameExtension getParentExtension() {
		return this.parentExtension;
	}

	public void setParentExtension(GameExtension ext) {
		this.parentExtension = ext;
	}

	public void sendResponse(Object cmd, IGameObject params, List<User> recipients) {
		this.parentExtension.send(cmd, params, recipients, false);
	}

	public void sendResponse(Object cmd, IGameObject params, User recipient) {
		this.parentExtension.send(cmd, params, recipient, false);
	}

}