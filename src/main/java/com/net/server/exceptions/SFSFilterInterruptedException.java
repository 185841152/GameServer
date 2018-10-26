package com.net.server.exceptions;

public class SFSFilterInterruptedException extends SFSRuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SFSFilterInterruptedException() {
	}

	public SFSFilterInterruptedException(String errorMsg) {
		super(errorMsg);
	}
}