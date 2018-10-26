package com.net.engine.events;

public abstract interface IEvent {
	public abstract Object getTarget();

	public abstract void setTarget(Object paramObject);

	public abstract String getName();

	public abstract void setName(String paramString);

	public abstract Object getParameter(String paramString);

	public abstract void setParameter(String paramString, Object paramObject);
}