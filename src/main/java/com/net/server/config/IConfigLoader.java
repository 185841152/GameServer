package com.net.server.config;

import java.io.FileNotFoundException;
import java.util.List;

import com.net.server.exceptions.GameException;

public abstract interface IConfigLoader {
	public abstract CoreSettings loadCoreSettings() throws FileNotFoundException;

	public abstract ServerSettings loadServerSettings() throws FileNotFoundException;

	public abstract List<ZoneSettings> loadZonesConfiguration() throws GameException;
	
}