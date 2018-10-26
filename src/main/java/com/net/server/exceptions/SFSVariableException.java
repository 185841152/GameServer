package com.net.server.exceptions;

public class SFSVariableException extends GameException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SFSVariableException() {
	}

	public SFSVariableException(String message) {
		super(message);
	}

	public SFSVariableException(String message, ErrorData data) {
		super(message, data);
	}
}