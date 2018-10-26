package com.net.engine.websocket.boot;

import com.net.engine.io.IProtocolCodec;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.ssl.SslContext;

public class WebSocketChildChannelHandler extends ChannelInitializer<SocketChannel> {
	private static final String WEBSOCKET_PATH = "/websocket";
//	private final IProtocolCodec codec;
	private final SslContext sslCtx;

	public WebSocketChildChannelHandler(IProtocolCodec codec, SslContext sslCtx) {
		this.sslCtx = sslCtx;
//		this.codec = codec;
	}

	@Override
	protected void initChannel(SocketChannel sc) throws Exception {
		ChannelPipeline pipeline = sc.pipeline();
		if (sslCtx != null) {
			pipeline.addLast(sslCtx.newHandler(sc.alloc()));
		}
		pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(65536));
        pipeline.addLast(new WebSocketServerCompressionHandler());
        pipeline.addLast(new WebSocketServerProtocolHandler(WEBSOCKET_PATH, null, true));
        pipeline.addLast(new WebSocketIndexPageHandler());
        pipeline.addLast(new WebSocketFrameHandler());
	}

}
