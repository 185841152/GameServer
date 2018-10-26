package com.net.server.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.net.server.exceptions.GameException;
import com.net.server.exceptions.SFSRuntimeException;
import com.thoughtworks.xstream.XStream;

public class DefaultConfigLoader implements IConfigLoader {
	private final XMLConfigHelper xmlHelper;
	private final Logger log;

	public DefaultConfigLoader() {
		this.xmlHelper = new XMLConfigHelper();
		this.log = LoggerFactory.getLogger(getClass());
	}

	public CoreSettings loadCoreSettings() throws FileNotFoundException {
		FileInputStream inStream = new FileInputStream(DefaultConstants.CORE_CFG_FILE);
		this.log.info("Loading: "+DefaultConstants.CORE_CFG_FILE);
		XStream xstream = new XStream();
		xstream.alias("coreSettings", CoreSettings.class);

		return (CoreSettings) xstream.fromXML(inStream);
	}

	public ServerSettings loadServerSettings() throws FileNotFoundException {
		FileInputStream inStream = new FileInputStream(this.xmlHelper.getServerConfigFileName());

		return (ServerSettings) this.xmlHelper.getServerXStreamDefinitions().fromXML(inStream);
	}
	
	

	public synchronized List<ZoneSettings> loadZonesConfiguration() throws GameException {
		List<ZoneSettings> zonesSettings = new ArrayList<ZoneSettings>();
		List<File> zoneDefinitionFiles = getZoneDefinitionFiles(DefaultConstants.ZONES_FOLDER);

		for (File file : zoneDefinitionFiles) {
			try {
				FileInputStream inStream = new FileInputStream(file);

				this.log.info("Loading: " + file.toString());
				zonesSettings.add((ZoneSettings) this.xmlHelper.getZonesXStreamDefinitions().fromXML(inStream));
			} catch (FileNotFoundException e) {
				throw new SFSRuntimeException("Could not locate Zone definition file: " + file.getAbsolutePath());
			}
		}

		return zonesSettings;
	}
	
	private List<File> getZoneDefinitionFiles(String path) throws GameException {
		List<File> files = new ArrayList<File>();

		File currDir = new File(path);
		if (currDir.isDirectory()) {
			for (File f : currDir.listFiles()) {
				if (f.getName().endsWith(DefaultConstants.ZONE_FILE_EXTENSION)) {
					files.add(f);
				}
			}
		} else {
			throw new GameException("Invalid zones definition folder: " + currDir);
		}

		return files;
	}
}