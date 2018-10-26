package com.net.engine.service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import com.net.engine.events.IEvent;
import com.net.engine.events.IEventDispatcher;
import com.net.engine.events.IEventListener;

/**
 * 服务基类
 * 
 * @author sunjian
 *
 */
public abstract class BaseCoreService implements IService, IEventDispatcher {
	private String serviceName;
	private Map<String, Set<IEventListener>> listenersByEvent;

	public BaseCoreService() {
		this.listenersByEvent = new ConcurrentHashMap<String, Set<IEventListener>>();
	}

	public void init(Object o) {
	}

	public void destroy(Object o) {
		this.listenersByEvent.clear();
	}

	public String getName() {
		return this.serviceName;
	}

	public void setName(String name) {
		this.serviceName = name;
	}

	public void handleMessage(Object message) {
	}

	public synchronized void addEventListener(String eventType, IEventListener listener) {
		Set<IEventListener> listeners = this.listenersByEvent.get(eventType);

		if (listeners == null) {
			listeners = new CopyOnWriteArraySet<IEventListener>();
			this.listenersByEvent.put(eventType, listeners);
		}

		listeners.add(listener);
	}

	public boolean hasEventListener(String eventType) {
		boolean found = false;
		Set<IEventListener> listeners = this.listenersByEvent.get(eventType);

		if ((listeners != null) && (listeners.size() > 0)) {
			found = true;
		}
		return found;
	}

	public void removeEventListener(String eventType, IEventListener listener) {
		Set<IEventListener> listeners = this.listenersByEvent.get(eventType);

		if (listeners != null)
			listeners.remove(listener);
	}

	public void dispatchEvent(IEvent event) {
		Set<IEventListener> listeners = this.listenersByEvent.get(event.getName());

		if ((listeners != null) && (listeners.size() > 0)) {
			for (IEventListener listenerObj : listeners) {
				listenerObj.handleEvent(event);
			}
		}
	}
}