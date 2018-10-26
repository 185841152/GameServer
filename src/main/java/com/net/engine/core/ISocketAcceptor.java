package com.net.engine.core;

import java.io.IOException;
import java.util.List;

import com.net.engine.config.SocketConfig;
import com.net.engine.core.security.IConnectionFilter;
import com.net.engine.data.BindableSocket;

import io.netty.channel.Channel;

public abstract interface ISocketAcceptor {
	public abstract void bindSocket(SocketConfig paramSocketConfig) throws IOException;

	public abstract List<BindableSocket> getBoundSockets();

	public abstract void handleAcceptableConnections(Channel channel);

	public abstract IConnectionFilter getConnectionFilter();

	public abstract void setConnectionFilter(IConnectionFilter paramIConnectionFilter);
}