package com.net.engine.exceptions;

public class RefusedAddressException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4061877230689292113L;

	public RefusedAddressException() {
	}

	public RefusedAddressException(String message) {
		super(message);
	}
}