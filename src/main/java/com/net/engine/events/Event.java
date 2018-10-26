package com.net.engine.events;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Event implements IEvent {
	protected Object target;
	protected String name;
	protected Map<String, Object> params;

	public Event(String name) {
		this.name = name;
	}

	public Event(String name, Object source) {
		this.target = source;
		this.name = name;
	}

	public Object getTarget() {
		return this.target;
	}

	public String getName() {
		return this.name;
	}

	public void setTarget(Object target) {
		this.target = target;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Object getParameter(String key) {
		Object param = null;

		if (this.params != null) {
			param = this.params.get(key);
		}
		return param;
	}

	public void setParameter(String key, Object value) {
		if (this.params == null) {
			this.params = new ConcurrentHashMap<String, Object>();
		}
		this.params.put(key, value);
	}

	public String toString() {
		return "Event { Name:" + this.name + ", Source: " + this.target + ", Params: " + this.params + " }";
	}
}