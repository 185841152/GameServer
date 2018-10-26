package com.net.engine.core;

import com.net.engine.config.SocketConfig;
import com.net.engine.core.security.DefaultConnectionFilter;
import com.net.engine.core.security.IConnectionFilter;
import com.net.engine.data.BindableSocket;
import com.net.engine.data.TransportType;
import com.net.engine.events.Event;
import com.net.engine.exceptions.RefusedAddressException;
import com.net.engine.handler.ChildChannelHandler;
import com.net.engine.service.BaseCoreService;
import com.net.engine.sessions.ISession;
import com.net.engine.sessions.ISessionManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class SocketAcceptor extends BaseCoreService implements ISocketAcceptor {
	private final NetEngine engine;
	private final Logger logger;
	private List<BindableSocket> boundSockets;
	private IConnectionFilter connectionFilter;
	private ISessionManager sessionManager;

	// 服务器线程组 用于网络事件的处理 一个用于服务器接收客户端的连接
	// 另一个线程组用于处理SocketChannel的网络读写
	EventLoopGroup bossGroup = null;;
	EventLoopGroup workerGroup = null;

	public SocketAcceptor() {

		this.engine = NetEngine.getInstance();
		this.logger = LoggerFactory.getLogger(SocketAcceptor.class);

		bossGroup = new NioEventLoopGroup();
		workerGroup = new NioEventLoopGroup();
		
		this.boundSockets = new ArrayList<BindableSocket>();
		this.connectionFilter = new DefaultConnectionFilter();
	}

	public void init(Object o) {
		super.init(o);
		this.sessionManager = this.engine.getSessionManager();
	}

	public void destroy(Object o) {
		super.destroy(o);

		bossGroup.shutdownGracefully();
		workerGroup.shutdownGracefully();

	}

	public void handleAcceptableConnections(Channel channel) {
		try {
			InetSocketAddress iAddr = (InetSocketAddress) channel.remoteAddress();
			if (iAddr == null) {
				return;
			}
			this.connectionFilter.validateAndAddAddress(iAddr.toString());

			ISession session = this.sessionManager.createSession(channel);

			this.sessionManager.addSession(session);

			Event sessionAddedEvent = new Event("sessionAdded");
			sessionAddedEvent.setParameter("session", session);
			dispatchEvent(sessionAddedEvent);
		} catch (RefusedAddressException e) {
			this.logger.info("Refused connection. " + e.getMessage());
			channel.close();
		}
	}

	public void bindSocket(SocketConfig socketConfig) throws IOException {
		if (socketConfig.getType() == TransportType.TCP) {
			bindTcpSocket(socketConfig.getAddress(), socketConfig.getPort());
		} else if (socketConfig.getType() == TransportType.UDP) {
			bindUdpSocket(socketConfig.getAddress(), socketConfig.getPort());
		} else
			throw new UnsupportedOperationException("Invalid transport type!");
	}

	public List<BindableSocket> getBoundSockets() {
		ArrayList<BindableSocket> list = null;

		synchronized (this.boundSockets) {
			list = new ArrayList<BindableSocket>(this.boundSockets);
		}

		return list;
	}

	public IConnectionFilter getConnectionFilter() {
		return this.connectionFilter;
	}

	public void setConnectionFilter(IConnectionFilter filter) {
		if (this.connectionFilter != null) {
			throw new IllegalStateException("A connection filter already exists!");
		}
		this.connectionFilter = filter;
	}

	private void bindTcpSocket(String address, int port) throws IOException {
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup);
			b.channel(NioServerSocketChannel.class);
			b.option(ChannelOption.SO_BACKLOG, 1024);
			b.option(ChannelOption.TCP_NODELAY, true);
			b.option(ChannelOption.SO_KEEPALIVE, true);
			b.childHandler(new ChildChannelHandler());

			// 绑定端口
			ChannelFuture f = b.bind(port).sync();
			if (f.isSuccess()) {
				logger.info("成功绑定端口{}:{}",address,port);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void bindUdpSocket(String address, int port) throws IOException {
		
	}
}