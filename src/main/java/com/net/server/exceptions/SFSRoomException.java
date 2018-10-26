package com.net.server.exceptions;

public class SFSRoomException extends GameException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SFSRoomException() {
	}

	public SFSRoomException(String message) {
		super(message);
	}

	public SFSRoomException(String message, ErrorData data) {
		super(message, data);
	}
}