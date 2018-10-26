package com.net.business.extensions.core;

import java.util.List;

import com.net.server.api.IGameApi;
import com.net.server.data.IGameObject;
import com.net.server.entities.User;

public abstract class BaseServerEventHandler implements IServerEventHandler {
	private GameExtension parentExtension;

	public GameExtension getParentExtension() {
		return this.parentExtension;
	}

	public void setParentExtension(GameExtension ext) {
		this.parentExtension = ext;
	}

	protected IGameApi getApi() {
		return this.parentExtension.sfsApi;
	}

	protected void send(Object cmdName, IGameObject params, User recipient) {
		this.parentExtension.send(cmdName, params, recipient);
	}

	protected void send(Object cmdName, IGameObject params, List<User> recipients) {
		this.parentExtension.send(cmdName, params, recipients);
	}

	protected void send(Object cmdName, IGameObject params, User recipient, boolean useUDP) {
		this.parentExtension.send(cmdName, params, recipient, useUDP);
	}

	protected void send(Object cmdName, IGameObject params, List<User> recipients, boolean useUDP) {
		this.parentExtension.send(cmdName, params, recipients, useUDP);
	}

	protected void trace(Object[] args) {
		this.parentExtension.trace(args);
	}

	protected void trace(ExtensionLogLevel level, Object[] args) {
		this.parentExtension.trace(level, args);
	}

}