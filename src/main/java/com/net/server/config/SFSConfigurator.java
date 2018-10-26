package com.net.server.config;

import java.io.FileNotFoundException;
import java.util.List;

import com.net.server.exceptions.GameException;

public final class SFSConfigurator implements IConfigurator {
	private volatile CoreSettings coreSettings;
	private volatile ServerSettings serverSettings;
	private volatile List<ZoneSettings> zonesSettings;
	private IConfigLoader cLoader;

	public SFSConfigurator() {
		initalizeConfigurator();
	}

	private void initalizeConfigurator() {
		this.cLoader = ConfigStorageFactory.getLoader();
	}

	public void loadConfiguration() throws FileNotFoundException {
		this.coreSettings = this.cLoader.loadCoreSettings();
		this.serverSettings = this.cLoader.loadServerSettings();

		if (this.serverSettings.webSocket == null)
			this.serverSettings.webSocket = new ServerSettings.WebSocketEngineSettings();
	}

	public synchronized List<ZoneSettings> loadZonesConfiguration() throws GameException {
		this.zonesSettings = this.cLoader.loadZonesConfiguration();
		return this.zonesSettings;
	}
	
	public CoreSettings getCoreSettings() {
		return this.coreSettings;
	}

	public synchronized ServerSettings getServerSettings() {
		return this.serverSettings;
	}

}