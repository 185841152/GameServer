package com.net.server.exceptions;

public class GameRuntimeException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4601967806503086546L;

	public GameRuntimeException() {
	}

	public GameRuntimeException(String message) {
		super(message);
	}

	public GameRuntimeException(Throwable t) {
		super(t);
	}
}