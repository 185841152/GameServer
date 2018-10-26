package com.net.engine.websocket.boot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.net.engine.io.IProtocolCodec;
import com.net.engine.websocket.WebSocketConfig;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

public class WebSocketBoot {
	private final WebSocketConfig config;
	private final IProtocolCodec protocolCodec;
	private final Logger bootLogger;

	public WebSocketBoot(WebSocketConfig cfg, IProtocolCodec codec) {
		this.bootLogger = LoggerFactory.getLogger("bootLogger");
		this.config = cfg;
		this.protocolCodec = codec;

		if (!cfg.isActive()) {
			return;
		}

		try {
			boot();
			this.bootLogger.info("WebSocket服务已启动:" + this.config.getHost() + ":" + this.config.getPort());
		} catch (Exception problem) {
			this.bootLogger.error("启动WebSocket服务失败:" + problem.getMessage());
		}
	}

	private void boot() throws Exception {
		// 服务器线程组 用于网络事件的处理 一个用于服务器接收客户端的连接
		// 另一个线程组用于处理SocketChannel的网络读写
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap();

			int webSocketPort = this.config.getPort();
			// Configure SSL.
			final SslContext sslCtx;
			if (this.config.isSSL()) {
				webSocketPort = this.config.getSslPort();
				SelfSignedCertificate ssc = new SelfSignedCertificate();
				sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
			} else {
				sslCtx = null;
			}

			b.group(bossGroup, workerGroup);
			b.channel(NioServerSocketChannel.class);
			b.option(ChannelOption.SO_BACKLOG, 1024);
			b.childHandler(new WebSocketChildChannelHandler(this.protocolCodec, sslCtx));

			// 绑定端口
			b.bind(webSocketPort).sync();
		} catch (Exception e) {
			this.bootLogger.error("启动WebSocket服务失败:", e);
		}
	}
}