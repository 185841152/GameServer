package com.net.server.entities.managers;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.net.business.extensions.ExtensionMain;
import com.net.business.extensions.core.IGameExtension;
import com.net.server.GameServer;
import com.net.server.core.ISFSEvent;
import com.net.server.core.ISFSEventListener;
import com.net.server.core.ISFSEventManager;
import com.net.server.core.SFSEventType;

public final class ExtensionManager implements ISFSEventListener {

	private final Logger logger = LoggerFactory.getLogger(ExtensionManager.class);

	private final Map<SFSEventType, Set<ISFSEventListener>> eventListeners = new ConcurrentHashMap<SFSEventType, Set<ISFSEventListener>>();
	// private final ConcurrentMap<Zone, IGameExtension> extensionsByZone;
	private IGameExtension extension;

	private GameServer server;
	private ISFSEventManager eventManager;
	private boolean inited;

	public ExtensionManager() {
		inited = false;
//		this.extensionsByZone = new ConcurrentHashMap<Zone, IGameExtension>();
	}

	public synchronized void init() {
		if (!inited) {
			server = GameServer.getInstance();
			eventManager = server.getEventManager();
			SFSEventType asfseventtype[] = SFSEventType.values();
			int j = asfseventtype.length;
			for (int i = 0; i < j; i++) {
				SFSEventType type = asfseventtype[i];
				eventManager.addEventListener(type, this);
			}
			IGameExtension extension = new ExtensionMain();
			extension.init();
			
			this.extension=extension;
			inited = true;
			logger.debug("Extension Manager started.");
		}
	}

//	public void createExtension(Zone parentZone, Room parentRoom) throws SFSExtensionException {
//		IGameExtension extension = new ExtensionMain();
//		extension.setParentZone(parentZone);
//		extension.init();
//
//		addExtension(extension);
//
//		parentZone.setExtension(extension);
//	}

//	public void addExtension(IGameExtension extension) {
//		this.extensionsByZone.put(extension.getParentZone(), extension);
//	}
//
//	public IGameExtension getZoneExtension(Zone zone) {
//		return this.extensionsByZone.get(zone);
//	}

	public synchronized void addExtensionEventListener(SFSEventType type, ISFSEventListener listener) {
		Set<ISFSEventListener> listeners = eventListeners.get(type);
		if (listeners == null) {
			listeners = new CopyOnWriteArraySet<ISFSEventListener>();
			eventListeners.put(type, listeners);
		}
		listeners.add(listener);
	}

	public void removeEventListener(SFSEventType type, ISFSEventListener listener) {
		Set<ISFSEventListener> listeners = eventListeners.get(type);
		if (listeners != null) {
			listeners.remove(listener);
		}
	}

	public void handleServerEvent(ISFSEvent event) throws Exception {
		Set<ISFSEventListener> listeners = eventListeners.get(event.getType());
		dispatchEvent(event, listeners);
	}

	private void dispatchEvent(ISFSEvent event, Collection<ISFSEventListener> listeners) {
		if (listeners != null && listeners.size() > 0) {
			for (Iterator<ISFSEventListener> iterator = listeners.iterator(); iterator.hasNext();) {
				ISFSEventListener listener = (ISFSEventListener) iterator.next();
				try {
					listener.handleServerEvent(event);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public IGameExtension getExtension() {
		return extension;
	}

	public void setExtension(IGameExtension extension) {
		this.extension = extension;
	}

}