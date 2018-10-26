package com.net.server.exceptions;

public class GameException extends Exception {
	private static final long serialVersionUID = 6052949605652105170L;
	ErrorData errorData;

	public GameException() {
		this.errorData = null;
	}

	public GameException(String message) {
		super(message);
		this.errorData = null;
	}

	public GameException(String message, ErrorData data) {
		super(message);
		this.errorData = data;
	}

	public GameException(Throwable t) {
		super(t);
		this.errorData = null;
	}

	public ErrorData getErrorData() {
		return this.errorData;
	}
}