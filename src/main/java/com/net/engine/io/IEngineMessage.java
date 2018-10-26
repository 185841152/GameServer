package com.net.engine.io;

public abstract interface IEngineMessage {
	public abstract Object getId();

	public abstract void setId(Object paramObject);

	public abstract Object getContent();

	public abstract void setContent(Object paramObject);

	public abstract Object getAttribute(String paramString);

	public abstract void setAttribute(String paramString, Object paramObject);
}