package com.net.game.client;

import com.net.engine.io.protocols.MessageDecoder;
import com.net.engine.io.protocols.MessageEncoder;
import com.net.game.client.Client.ConnectionCallBack;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

public class ClientChannelHandler extends ChannelInitializer<SocketChannel>{

	private ConnectionCallBack callBack;
	public ClientChannelHandler(ConnectionCallBack callBack) {
		this.callBack=callBack;
	}
	@Override
	protected void initChannel(SocketChannel sc) throws Exception {
		sc.pipeline().addLast("frameDecoder", new MessageDecoder());
		sc.pipeline().addLast("frameEncoder", new MessageEncoder());
		sc.pipeline().addLast(new ClientHandler(callBack));
	}
	

}
