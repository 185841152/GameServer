package com.net.server.exceptions;

public class SFSCreateRoomException extends GameException {
	private static final long serialVersionUID = 4751733417642191809L;

	public SFSCreateRoomException() {
	}

	public SFSCreateRoomException(String message) {
		super(message);
	}

	public SFSCreateRoomException(String message, ErrorData data) {
		super(message, data);
	}
}