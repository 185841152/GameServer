package com.net.business.extensions.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameHandlerFactory implements IHandlerFactory {
	private final Map<Object, Class<?>> handlers;
	private final Map<Object, Object> cachedHandlers;
	private final GameExtension parentExtension;

	public GameHandlerFactory(GameExtension parentExtension) {
		this.handlers = new ConcurrentHashMap<Object, Class<?>>();
		this.cachedHandlers = new ConcurrentHashMap<Object, Object>();
		this.parentExtension = parentExtension;
	}

	public void addHandler(Object handlerKey, Class<?> handlerClass) {
		this.handlers.put(handlerKey, handlerClass);
	}

	public void addHandler(Object handlerKey, Object requestHandler) {
		setHandlerParentExtension(requestHandler);
		this.cachedHandlers.put(handlerKey, requestHandler);
	}

	public synchronized void clearAll() {
		this.handlers.clear();
		this.cachedHandlers.clear();
	}

	public synchronized void removeHandler(Object handlerKey) {
		this.handlers.remove(handlerKey);

		if (this.cachedHandlers.containsKey(handlerKey))
			this.cachedHandlers.remove(handlerKey);
	}

	public Object findHandler(Object key) throws InstantiationException, IllegalAccessException {
		Object handler = getHandlerInstance(key);

		return handler;
	}

	private Object getHandlerInstance(Object key) throws InstantiationException, IllegalAccessException {
		Object handler = this.cachedHandlers.get(key);

		if (handler != null) {
			return handler;
		}

		Class<?> handlerClass = this.handlers.get(key);

		if (handlerClass == null) {
			return null;
		}

		handler = handlerClass.newInstance();

		setHandlerParentExtension(handler);

		this.cachedHandlers.put(key, handler);
		return handler;
	}

	private void setHandlerParentExtension(Object handler) {
		if ((handler instanceof IClientRequestHandler)) {
			((IClientRequestHandler) handler).setParentExtension(this.parentExtension);
		} else if ((handler instanceof IServerEventHandler))
			((IServerEventHandler) handler).setParentExtension(this.parentExtension);
	}
}