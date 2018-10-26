package com.net.server.core;

public abstract interface ISFSEvent {
	public abstract SFSEventType getType();

	public abstract Object getParameter(ISFSEventParam paramISFSEventParam);
}