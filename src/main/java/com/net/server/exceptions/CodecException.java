package com.net.server.exceptions;

public class CodecException extends GameException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2997018875452303079L;

	public CodecException() {
	}

	public CodecException(String message) {
		super(message);
	}

	public CodecException(Throwable t) {
		super(t);
	}
}