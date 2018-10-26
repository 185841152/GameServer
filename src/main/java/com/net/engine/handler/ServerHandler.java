package com.net.engine.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.net.engine.core.ISocketAcceptor;
import com.net.engine.core.ISocketReader;
import com.net.engine.core.NetEngine;
import com.net.engine.data.IPacket;
import com.net.engine.data.Packet;
import com.net.engine.data.TransportType;
import com.net.engine.io.protocols.ProtocolType;
import com.net.engine.sessions.ISession;
import com.net.engine.sessions.ISessionManager;
import com.net.engine.sessions.Session;
import com.net.server.data.GameObject;
import com.net.server.data.IGameObject;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.ReadTimeoutException;

public class ServerHandler extends ChannelInboundHandlerAdapter{
	private static Logger logger=LoggerFactory.getLogger(ServerHandler.class);
	
	private final NetEngine engine;
	private ISocketAcceptor socketAcceptor;
	private ISessionManager sessionManager;
	private ISocketReader socketReader;
	
	public ServerHandler() {
		this.engine = NetEngine.getInstance();
		this.socketAcceptor = this.engine.getSocketAcceptor();
		this.sessionManager=this.engine.getSessionManager();
		this.socketReader=this.engine.getSocketReader();
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		this.socketAcceptor.handleAcceptableConnections(ctx.channel());
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		this.sessionManager.onSocketDisconnected(ctx.channel());
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		IPacket packet=(IPacket) msg;
		//将session放入packet
		ISession session=sessionManager.getLocalSessionByConnection(ctx.channel());
		session.setSystemProperty(Session.PROTOCOL, packet.getProtocolType());
		session.setLastReadTime(System.currentTimeMillis());
		packet.setSender(session);
		this.socketReader.onPacketRead(packet);
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		super.channelReadComplete(ctx);
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		super.channelRegistered(ctx);
	}

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		ctx.close();
		super.channelUnregistered(ctx);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)throws Exception {
		logger.error("Work线程池系统错误", cause);
		if (cause instanceof ReadTimeoutException) {
		}else {
			ctx.close();
		}
	}
}
