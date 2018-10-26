package com.net.server.exceptions;

public class SFSBuddyListException extends GameException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SFSBuddyListException() {
	}

	public SFSBuddyListException(String message) {
		super(message);
	}

	public SFSBuddyListException(String message, ErrorData data) {
		super(message, data);
	}
}