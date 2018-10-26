package com.net.server.exceptions;

public class SFSLoginException extends GameException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SFSLoginException() {
	}

	public SFSLoginException(String message) {
		super(message);
	}

	public SFSLoginException(String message, ErrorData data) {
		super(message, data);
	}
}