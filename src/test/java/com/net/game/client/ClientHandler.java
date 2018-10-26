package com.net.game.client;

import com.net.engine.data.IPacket;
import com.net.engine.data.Packet;
import com.net.engine.data.TransportType;
import com.net.engine.io.protocols.ProtocolType;
import com.net.game.client.Client.ConnectionCallBack;
import com.net.server.data.GameObject;
import com.net.server.data.IGameObject;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.ReadTimeoutException;

public class ClientHandler extends ChannelInboundHandlerAdapter {

	private ConnectionCallBack callBack;

	public ClientHandler(ConnectionCallBack callBack) {
		this.callBack = callBack;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("channelActive");
		// 发送登录消息
		IGameObject gameObject = GameObject.newInstance();
		IGameObject params = GameObject.newInstance();
		params.putUtfString("u", "sundawang");//570035d06c03023b0f4be37f46ca234349146f477
		params.putUtfString("z", "BasicExamples");
		params.putInt("si", 1);
		params.putUtfString("ip", "127.0.0.1");
		params.putUtfString("t", "dawangjiaowolaixunshan");
		gameObject.putShort("a", new Short("1"));
		gameObject.putGameObject("p", params);
		
		IPacket packet = new Packet();
		packet.setTransportType(TransportType.TCP);
		packet.setData(gameObject);
		packet.setProtocolType(ProtocolType.BINARY);
		
		ctx.writeAndFlush(packet);

	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("=================channelInactive==============");
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		System.out.println("channelRead");
		IPacket packet = (IPacket) msg;
		IGameObject result = (IGameObject) packet.getData();
		Short cmd = result.getShort("a");
		// 如果是登录消息，则发送加载角色信息
		if (cmd.intValue() == 1) {
//			// 发送加载角色信息消息
//			IGameObject gameObject = GameObject.newInstance();
//			IGameObject params = GameObject.newInstance();
//			params.putInt("ri", 1);
//			gameObject.putShort("a", new Short("3"));
//			
//			gameObject.putGameObject("p", params);
//			
//			IPacket newpacket = new Packet();
//			newpacket.setTransportType(TransportType.TCP);
//			newpacket.setData(gameObject);
//			newpacket.setProtocolType(ProtocolType.BINARY);
//			
//			ctx.writeAndFlush(newpacket);
			
			callBack.loadPlayerSuccess(result.getGameObject("p"), ctx.channel());
		}else if (cmd.intValue() == 3) {
			
		}else {
			callBack.onServerResponse(result, ctx.channel());
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		System.out.println("channelReadComplete");
		super.channelReadComplete(ctx);
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		System.out.println("channelRegistered");
		super.channelRegistered(ctx);
	}

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		System.out.println("channelUnregistered");
		super.channelUnregistered(ctx);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		if (cause instanceof ReadTimeoutException) {
			System.out.println("readTimeOut");
		} else {
			ctx.close();
		}
	}
}
