package com.net.game.client;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.net.engine.data.IPacket;
import com.net.engine.data.Packet;
import com.net.engine.data.TransportType;
import com.net.engine.io.protocols.ProtocolType;
import com.net.game.client.Client.ConnectionCallBack;
import com.net.server.data.GameObject;
import com.net.server.data.IGameObject;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class ClusterClient {
	private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
	private Channel channel;

	public void connect(int inetPort, String inetHost, ConnectionCallBack playerSuccess) throws Exception {

		EventLoopGroup group = new NioEventLoopGroup();

		try {

			Bootstrap b = new Bootstrap();

			b.group(group).channel(NioSocketChannel.class);
			b.option(ChannelOption.TCP_NODELAY, true);
			b.handler(new ClientChannelHandler(playerSuccess));
			// 发起异步连接操作
			ChannelFuture f = b.connect(inetHost, inetPort);
			this.channel = f.channel();
			// 等待客户端链路关闭
			f.channel().closeFuture().sync();
		} finally {
			group.shutdownGracefully();
			// 释放完资源后重新发起连接,5秒钟一次
			executor.execute(new Runnable() {
				public void run() {
					try {
						TimeUnit.SECONDS.sleep(5);
						try {
							connect(inetPort, inetHost,playerSuccess);
						} catch (Exception e) {
							e.printStackTrace();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
	}

	public void sendExt(IGameObject params, Short cmd) {
		// 发送登录消息
		IGameObject gameObject = GameObject.newInstance();
		gameObject.putShort("a", cmd);
		gameObject.putGameObject("p", params);

		IPacket packet = new Packet();
		packet.setTransportType(TransportType.TCP);
		packet.setData(gameObject);
		packet.setProtocolType(ProtocolType.BINARY);

		this.channel.writeAndFlush(packet);
	}
}
