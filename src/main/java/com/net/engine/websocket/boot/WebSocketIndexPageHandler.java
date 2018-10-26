package com.net.engine.websocket.boot;

import com.net.server.GameServer;
import com.net.server.controllers.SystemRequest;
import com.net.server.data.GameObject;
import com.net.server.data.IGameObject;
import com.net.server.entities.User;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Outputs index page content.
 */
public class WebSocketIndexPageHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
	private static Logger logger=LoggerFactory.getLogger(WebSocketIndexPageHandler.class);

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
		if (!req.decoderResult().isSuccess()) {
			sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
			return;
		}
		IGameObject result=GameObject.newInstance();
		// 解析参数
		Map<String, String> params = RequestParser.parse(req);
		logger.info("HttpRequest-{}",req.uri());
		//获取用户详情
		if (req.uri().startsWith("/sendOrderMessage")) {
			List<User> users=GameServer.getInstance().getUserManager().getAllUsers();
			if (users!=null && users.size()>0){
				IGameObject msg= GameObject.newInstance();
				String content=params.get("content");
				msg.putUtfString("head","https://wx.qlogo.cn/mmopen/vi_32/BiatyCuvfjVwbIFZfRyjyT1ua6lIOX9ibJHRksah3dH9wXhbamMt0cEYuszu1mDR0iaOJJKj4SXzgqWYyrbkjW9Dg/132");
				msg.putUtfString("content",content);
				GameServer.getInstance().getAPIManager().getSFSApi().sendExtensionResponse(SystemRequest.OnUserAddOrder.getId(),msg,users,null,false);
			}
		}else {
			sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND));
			return;
		}
		
		ByteBuf content=Unpooled.copiedBuffer(result.toJson(), CharsetUtil.UTF_8);;
		FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK, content);

		res.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
		HttpUtil.setContentLength(res, content.readableBytes());

		sendHttpResponse(ctx, req, res);
	}
	

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
		cause.printStackTrace();
		ctx.close();
	}

	private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
		if (res.status().code() != 200) {
			ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
			res.content().writeBytes(buf);
			buf.release();
			HttpUtil.setContentLength(res, res.content().readableBytes());
		}

		ChannelFuture f = ctx.channel().writeAndFlush(res);
		f.addListener(ChannelFutureListener.CLOSE);
	}
}
