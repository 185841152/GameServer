package com.net.server.util;

public abstract interface IResponseThrottler {
	public abstract void enqueueResponse(Object paramObject);

	public abstract void setInterval(int paramInt);

	public abstract int getInterval();

	public abstract String getName();
}