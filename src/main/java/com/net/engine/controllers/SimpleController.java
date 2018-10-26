package com.net.engine.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SimpleController implements IController {
	protected Object id;
	protected String name;
	protected volatile boolean isActive = false;
	protected final Logger bootLogger;
	protected final Logger logger;

	public SimpleController() {
		this.bootLogger = LoggerFactory.getLogger("bootLogger");
		this.logger = LoggerFactory.getLogger(getClass());
	}

	public void init(Object o) {
		if (this.isActive) {
			throw new IllegalArgumentException("Object is already initialized. Destroy it first!");
		}
		this.isActive = true;

		this.bootLogger.info(String.format("Controller started: %s ", new Object[] { getClass().getName() }));
	}

	public void destroy(Object o) {
		this.isActive = false;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		if (this.name != null) {
			throw new IllegalStateException("Controller already has a name: " + this.name);
		}
		this.name = name;
	}

	public Object getId() {
		return this.id;
	}

	public void setId(Object id) {
		if (this.id != null) {
			throw new IllegalStateException("Controller already has an id: " + this.id);
		}
		this.id = id;
	}

	public Logger getLogger() {
		return logger;
	}

}