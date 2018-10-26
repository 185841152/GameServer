package com.net.engine.events;

public abstract interface IEventDispatcher {
	public abstract void addEventListener(String paramString, IEventListener paramIEventListener);

	public abstract boolean hasEventListener(String paramString);

	public abstract void removeEventListener(String paramString, IEventListener paramIEventListener);

	public abstract void dispatchEvent(IEvent paramIEvent);
}