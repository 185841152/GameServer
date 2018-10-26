package com.net.server.config;

import com.net.server.core.ObjectFactory;
import com.net.server.exceptions.SFSRuntimeException;

public class ConfigStorageFactory extends ObjectFactory {
	private static final String DEFAULT_LOADER = "com.net.server.config.DefaultConfigLoader";
	private static final ConfigStorageFactory instance = new ConfigStorageFactory();

	public static IConfigLoader getLoader() {
		return getLoader(DEFAULT_LOADER);
	}

	public static IConfigLoader getLoader(String cName) {
		IConfigLoader loader = null;
		try {
			loader = (IConfigLoader) instance.loadClass(cName);
		} catch (Exception e) {
			throw new SFSRuntimeException(e);
		}

		return loader;
	}

}