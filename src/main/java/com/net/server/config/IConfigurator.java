package com.net.server.config;

import java.util.List;

import com.net.server.exceptions.GameException;

public abstract interface IConfigurator {
	public abstract void loadConfiguration() throws Exception;

	public abstract CoreSettings getCoreSettings();

	public abstract ServerSettings getServerSettings();

	public abstract List<ZoneSettings> loadZonesConfiguration() throws GameException;
	
}