package com.net.server.config;

import java.util.HashMap;
import java.util.Map;

import com.net.server.core.ObjectFactory;
import com.net.server.exceptions.SFSBootException;

public final class ServiceBootStrapper extends ObjectFactory {
	private static final ServiceBootStrapper _instance = new ServiceBootStrapper();
	private final Map<SFSService, String> servicesByKey;

	public static ServiceBootStrapper getInstance() {
		return _instance;
	}

	private ServiceBootStrapper() {
		this.servicesByKey = new HashMap<SFSService, String>();
		populateDefaults();
	}

	public void addService(SFSService serviceKey, String className) {
		this.servicesByKey.put(serviceKey, className);
	}

	public Object load(SFSService serviceKey) throws SFSBootException {
		String className = (String) this.servicesByKey.get(serviceKey);
		Object service = null;

		if (className == null) {
			throw new SFSBootException("No service definition found for: " + serviceKey + ". Service not found");
		}
		try {
			service = loadClass(className);
		} catch (Exception e) {
			throw new SFSBootException(String.format("Error while bootstrapping service: %s, %s. Cause: %s",
					new Object[] { serviceKey, className, e.toString() }));
		}

		return service;
	}

	private void populateDefaults() {
		this.servicesByKey.put(SFSService.BannedUserManager,
				"com.smartfoxserver.v2.entities.managers.SFSBannedUserManager");
		this.servicesByKey.put(SFSService.ZoneManager, "com.net.server.entities.managers.SFSZoneManager");
		this.servicesByKey.put(SFSService.RoomManager, "com.net.server.entities.managers.SFSRoomManager");
		this.servicesByKey.put(SFSService.BuddyManager, "com.net.server.buddylist.SFSBuddyListManager");
		this.servicesByKey.put(SFSService.Configurator, "com.net.server.config.SFSConfigurator");
	}
}