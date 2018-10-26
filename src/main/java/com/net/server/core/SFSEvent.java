package com.net.server.core;

import java.util.Map;

public class SFSEvent implements ISFSEvent {
	private final SFSEventType type;
	private final Map<ISFSEventParam, Object> params;

	public SFSEvent(SFSEventType type) {
		this(type, null);
	}

	public SFSEvent(SFSEventType type, Map<ISFSEventParam, Object> params) {
		this.type = type;
		this.params = params;
	}

	public SFSEventType getType() {
		return this.type;
	}

	public Object getParameter(ISFSEventParam id) {
		Object param = null;

		if (this.params != null) {
			param = this.params.get(id);
		}
		return param;
	}

	public String toString() {
		return String.format("{ %s, Params: %s }",
				new Object[] { this.type, this.params != null ? this.params.keySet() : "none" });
	}
}