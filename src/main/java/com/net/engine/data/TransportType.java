package com.net.engine.data;

public enum TransportType {
	TCP("Tcp"), UDP("Udp"), BLUEBOX("BlueBox");

	String name;

	private TransportType(String name) {
		this.name = name;
	}

	public String toString() {
		return "(" + this.name + ")";
	}

	public static TransportType fromName(String name) {
		for (TransportType tt : values()) {
			if (tt.name.equalsIgnoreCase(name)) {
				return tt;
			}
		}

		throw new IllegalArgumentException("There is no TransportType definition for the requested type: " + name);
	}
}