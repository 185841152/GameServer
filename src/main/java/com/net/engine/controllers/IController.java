package com.net.engine.controllers;

import com.net.engine.exceptions.RequestQueueFullException;
import com.net.engine.io.IRequest;
import com.net.engine.service.IService;

public abstract interface IController extends IService {
	public abstract Object getId();

	public abstract void setId(Object paramObject);

	public abstract void enqueueRequest(IRequest paramIRequest) throws RequestQueueFullException;

	public abstract int getQueueSize();

	public abstract int getMaxQueueSize();

	public abstract void setMaxQueueSize(int paramInt);

	public abstract int getThreadPoolSize();

	public abstract void setThreadPoolSize(int paramInt);
}