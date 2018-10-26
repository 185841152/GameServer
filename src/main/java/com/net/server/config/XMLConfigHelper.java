package com.net.server.config;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public final class XMLConfigHelper {
	public XStream getServerXStreamDefinitions() {
		XStream xstream = new XStream();
		xstream.alias("serverSettings", ServerSettings.class);

		xstream.alias("socket", ServerSettings.SocketAddress.class);
		xstream.useAttributeFor(ServerSettings.SocketAddress.class, "address");
		xstream.useAttributeFor(ServerSettings.SocketAddress.class, "port");
		xstream.useAttributeFor(ServerSettings.SocketAddress.class, "type");

		xstream.alias("address", ServerSettings.ClusterAddress.class);
		xstream.useAttributeFor(ServerSettings.ClusterAddress.class, "address");
		xstream.useAttributeFor(ServerSettings.ClusterAddress.class, "port");

		xstream.alias("ipFilter", ServerSettings.IpFilterSettings.class);
		xstream.alias("bannedUserManager", ServerSettings.BannedUserManagerSettings.class);
		xstream.alias("websocketEngine", ServerSettings.WebSocketEngineSettings.class);

		return xstream;
	}

	public String getServerConfigFileName() {
		return DefaultConstants.SERVER_CFG_FILE;
	}

	public XStream getZonesXStreamDefinitions() {
		XStream xstream = new XStream(new DomDriver());

		xstream.alias("zone", ZoneSettings.class);

		xstream.alias("room", ZoneSettings.RoomSettings.class);
		xstream.alias("MMOSettings", ZoneSettings.MMOSettings.class);
		return xstream;
	}
}