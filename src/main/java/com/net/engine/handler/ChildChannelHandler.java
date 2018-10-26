package com.net.engine.handler;

import com.net.engine.io.protocols.MessageDecoder;
import com.net.engine.io.protocols.MessageEncoder;
import com.net.server.GameServer;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

public class ChildChannelHandler extends ChannelInitializer<SocketChannel> {
	
	@Override
	protected void initChannel(SocketChannel sc) throws Exception {
		sc.pipeline().addLast("frameDecoder", new MessageDecoder());
		sc.pipeline().addLast("frameEncoder", new MessageEncoder());
//		sc.pipeline().addLast("readTimeOut", new ReadTimeoutHandler(20));
		sc.pipeline().addLast(new ServerHandler());
	}
}
