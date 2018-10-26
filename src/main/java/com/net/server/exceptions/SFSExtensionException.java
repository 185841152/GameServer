package com.net.server.exceptions;

public class SFSExtensionException extends GameException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3368792477878367221L;

	public SFSExtensionException() {
	}

	public SFSExtensionException(String message) {
		super(message);
	}

	public SFSExtensionException(String message, ErrorData data) {
		super(message, data);
	}
}