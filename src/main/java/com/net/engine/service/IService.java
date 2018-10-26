package com.net.engine.service;

public abstract interface IService {
	public abstract void init(Object paramObject);

	public abstract void destroy(Object paramObject);

	public abstract void handleMessage(Object paramObject);

	public abstract String getName();

	public abstract void setName(String paramString);
}