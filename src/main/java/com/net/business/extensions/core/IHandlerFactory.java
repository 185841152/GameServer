package com.net.business.extensions.core;

public abstract interface IHandlerFactory {
	public abstract void addHandler(Object paramString, Class<?> paramClass);

	public abstract void addHandler(Object paramString, Object paramObject);

	public abstract void removeHandler(Object paramString);

	public abstract Object findHandler(Object paramString) throws InstantiationException, IllegalAccessException;

	public abstract void clearAll();
}