package com.net.server.util;

public abstract interface IDisconnectionReason {
	public abstract int getValue();

	public abstract byte getByteValue();
}