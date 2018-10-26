package com.net.server.core;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.net.engine.service.BaseCoreService;
import com.net.server.GameServer;
import com.net.server.util.executor.NettyDefaultEventExecutorGroup;

public final class SFSEventManager extends BaseCoreService implements ISFSEventManager {
	private GameServer server;
	private NettyDefaultEventExecutorGroup systemThreadPool;
	private final Map<SFSEventType, Set<ISFSEventListener>> listenersByEvent;
	private final Logger logger;
	private boolean inited = false;

	public SFSEventManager() {
		setName("SFSEventManager");
		this.logger = LoggerFactory.getLogger(SFSEventManager.class);
		this.listenersByEvent = new ConcurrentHashMap<SFSEventType, Set<ISFSEventListener>>();
	}

	public synchronized void init(Object o) {
		if (!this.inited) {
			super.init(o);
			this.server=GameServer.getInstance();
			this.systemThreadPool = this.server.getSystemThreadPool();
			
			this.logger.info(" initalized");
			this.inited = true;
		}
	}

	public void destroy(Object o) {
		super.destroy(o);
		this.listenersByEvent.clear();
		this.logger.info(" shut down.");
	}

	public NettyDefaultEventExecutorGroup getThreadPool() {
		return this.systemThreadPool;
	}

	public synchronized void addEventListener(SFSEventType type, ISFSEventListener listener) {
		Set<ISFSEventListener> listeners = this.listenersByEvent.get(type);

		if (listeners == null) {
			listeners = new CopyOnWriteArraySet<ISFSEventListener>();
			this.listenersByEvent.put(type, listeners);
		}

		listeners.add(listener);
	}

	public boolean hasEventListener(SFSEventType type) {
		boolean found = false;

		Set<ISFSEventListener> listeners = this.listenersByEvent.get(type);
		if ((listeners != null) && (listeners.size() > 0)) {
			found = true;
		}
		return found;
	}

	public synchronized void removeEventListener(SFSEventType type, ISFSEventListener listener) {
		Set<ISFSEventListener> listeners = this.listenersByEvent.get(type);

		if (listeners != null)
			listeners.remove(listener);
	}

	public void dispatchEvent(ISFSEvent event) {
		Set<ISFSEventListener> listeners = this.listenersByEvent.get(event.getType());

		if ((listeners != null) && (listeners.size() > 0)) {
			for (ISFSEventListener listener : listeners) {
				this.systemThreadPool.execute(new SFSEventRunner(listener, event));
			}
		}
	}

	private static final class SFSEventRunner implements Runnable {
		private final ISFSEventListener listener;
		private final ISFSEvent event;

		public SFSEventRunner(ISFSEventListener listener, ISFSEvent event) {
			this.listener = listener;
			this.event = event;
		}

		public void run() {
			try {
				this.listener.handleServerEvent(this.event);
			} catch (Exception e) {
				LoggerFactory.getLogger(SFSEventManager.class).warn(
						"Error in event handler: " + e + ", Event: " + this.event + " Listener: " + this.listener);
			}
		}
	}
}