package com.net.server.core;

public abstract interface ISFSEventDispatcher {
	public abstract void addEventListener(SFSEventType paramSFSEventType, ISFSEventListener paramISFSEventListener);

	public abstract boolean hasEventListener(SFSEventType paramSFSEventType);

	public abstract void removeEventListener(SFSEventType paramSFSEventType, ISFSEventListener paramISFSEventListener);

	public abstract void dispatchEvent(ISFSEvent paramISFSEvent);
}