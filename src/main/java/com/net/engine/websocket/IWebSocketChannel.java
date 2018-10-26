package com.net.engine.websocket;

import java.net.SocketAddress;

public abstract interface IWebSocketChannel {
	public abstract void write(String paramString);

	public abstract SocketAddress getRemoteAddress();

	public abstract SocketAddress getLocalAddress();

	public abstract void close();
}