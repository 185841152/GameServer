package com.net.engine.core;

public abstract interface IMethodDispatcher {
	public abstract void registerMethod(String key, String methodName);

	public abstract void unregisterKey(String key);

	public abstract void callMethod(String key, Object[] params) throws Exception;
}