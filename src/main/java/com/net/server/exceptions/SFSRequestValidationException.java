package com.net.server.exceptions;

public class SFSRequestValidationException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SFSRequestValidationException() {
	}

	public SFSRequestValidationException(String message) {
		super(message);
	}

	public SFSRequestValidationException(Throwable t) {
		super(t);
	}
}