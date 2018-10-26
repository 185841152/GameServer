package com.net.server.core;

public abstract class ObjectFactory {
	protected Object loadClass(String className) throws Exception {
		Class<?> serviceClass = Class.forName(className);
		return serviceClass.newInstance();
	}
}