package com.net.server.exceptions;

public class SFSJoinRoomException extends GameException {
	private static final long serialVersionUID = 6384101728401558209L;

	public SFSJoinRoomException() {
	}

	public SFSJoinRoomException(String message) {
		super(message);
	}

	public SFSJoinRoomException(String message, ErrorData data) {
		super(message, data);
	}
}