package com.net.engine.controllers;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DefaultControllerManager implements IControllerManager {
	protected String name;
	protected ConcurrentMap<Object, IController> controllers;

	public DefaultControllerManager() {
		this.controllers = new ConcurrentHashMap<Object, IController>();
	}

	public void init(Object o) {
		startAllControllers();
	}

	public void destroy(Object o) {
		shutDownAllControllers();
		this.controllers = null;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void handleMessage(Object message) {
	}

	public void addController(Object id, IController controller) {
		this.controllers.putIfAbsent(id, controller);
	}

	public IController getControllerById(Object id) {
		return (IController) this.controllers.get(id);
	}

	public void removeController(Object id) {
		this.controllers.remove(id);
	}

	private synchronized void shutDownAllControllers() {
		for (IController controller : this.controllers.values()) {
			controller.destroy(null);
		}
	}

	private synchronized void startAllControllers() {
		for (IController controller : this.controllers.values()) {
			controller.init(null);
		}
	}
}