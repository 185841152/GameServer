package com.net.engine.exceptions;

public class MessageQueueFullException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MessageQueueFullException() {
	}

	public MessageQueueFullException(String message) {
		super(message);
	}
}