package com.net.engine.exceptions;

public class RequestQueueFullException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6008168634392165514L;

	public RequestQueueFullException() {
	}

	public RequestQueueFullException(String message) {
		super(message);
	}
}