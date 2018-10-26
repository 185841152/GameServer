package com.net.engine.controllers;

import com.net.engine.service.IService;

public abstract interface IControllerManager extends IService {
	public abstract IController getControllerById(Object paramObject);

	public abstract void addController(Object paramObject, IController paramIController);

	public abstract void removeController(Object paramObject);
}