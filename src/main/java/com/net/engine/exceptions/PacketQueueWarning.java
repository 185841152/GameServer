package com.net.engine.exceptions;

public class PacketQueueWarning extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 166984453269517621L;

	public PacketQueueWarning() {
	}

	public PacketQueueWarning(String message) {
		super(message);
	}
}