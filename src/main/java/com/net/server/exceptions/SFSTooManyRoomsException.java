package com.net.server.exceptions;

public class SFSTooManyRoomsException extends GameException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SFSTooManyRoomsException(String message) {
		super(message);
	}

	public SFSTooManyRoomsException(String message, ErrorData data) {
		super(message, data);
	}
}
