package com.net.engine.io.protocols;

public enum ProtocolType {
	BINARY("Binary"), TEXT("Text"), FLASH_CROSSDOMAIN_POLICY("Flash CrossDomain Policy");

	private String description;

	private ProtocolType(String description) {
		this.description = description;
	}

	public String toString() {
		return this.description;
	}
}