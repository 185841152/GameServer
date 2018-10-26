package com.net.server.util;

import com.net.server.GameServer;
import com.net.server.config.ServerSettings;

public class ClusterUtils {
	public static String generateNodeId() {
		ServerSettings settings = GameServer.getInstance().getConfigurator().getServerSettings();
		if (settings.socketAddresses.size() > 0) {
			for (int i = 0, j = settings.socketAddresses.size(); i < j; i++) {
				if (!settings.socketAddresses.get(i).address.equals("127.0.0.1")) {
					String nodeId = settings.socketAddresses.get(i).address + ":"
							+ settings.socketAddresses.get(i).port;
					return nodeId;
				}
			}
		}
		return "0";
	}

}
