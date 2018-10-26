package com.net.engine.websocket;

import com.net.engine.core.ISocketReader;
import com.net.engine.data.IPacket;
import com.net.engine.io.IRequest;
import com.net.engine.io.IResponse;
import com.net.engine.io.Request;
import com.net.engine.sessions.ISession;
import com.net.server.controllers.SystemRequest;
import com.net.server.data.GameObject;
import com.net.server.data.IGameObject;

public class WebSocketProtocolCodec extends ISocketReader {
	private static final String CONTROLLER_ID = "c";
	private static final String ACTION_ID = "a";
	private static final String PARAM_ID = "p";
	private final WebSocketStats webSocketStats;

	public WebSocketProtocolCodec(WebSocketStats wss) {
		this.webSocketStats = wss;
	}

	public void onPacketRead(IPacket packet) {
		if (packet == null) {
			throw new IllegalStateException("WebSocket null packet!");
		}
		IGameObject requestObject = null;

		if (packet.isTcp()) {
			String buff = (String) packet.getData();
			try {
				requestObject = GameObject.newFromJsonData(buff);
			} catch (Exception e) {
				this.logger.warn("Error deserializing request: " + e);
			}
		}

		if (requestObject != null) {
			if (this.logger.isDebugEnabled()) {
				this.logger.debug(requestObject.getDump());
			}

			dispatchRequest(requestObject, packet);
		}
	}

	public void onPacketWrite(IResponse response) {
		IGameObject packet = GameObject.newInstance();

		packet.putShort(ACTION_ID, ((Short) response.getId()).shortValue());

		packet.putGameObject(PARAM_ID, (IGameObject) response.getContent());

		if ((response.getRecipients()!=null) && (this.logger.isDebugEnabled())) {
			this.logger.debug("{OUT}: " + SystemRequest.fromId(response.getId()));
		}
		String rawPacket = packet.toJson();

		int bytesLen = rawPacket.length();
		ISession session=response.getRecipients();
		
		IWebSocketChannel channel = (IWebSocketChannel) session.getSystemProperty("wsChannel");
		channel.write(rawPacket);

		this.webSocketStats.addWrittenPackets(1);
		this.webSocketStats.addWrittenBytes(bytesLen);
	}

	private void dispatchRequest(IGameObject requestObject, IPacket packet) {
		if (requestObject.isNull(CONTROLLER_ID)) {
			throw new IllegalStateException("Request rejected: No Controller ID in request!");
		}

		if (requestObject.isNull(ACTION_ID)) {
			throw new IllegalStateException("Request rejected: No Action ID in request!");
		}
		if (requestObject.isNull(PARAM_ID)) {
			throw new IllegalStateException("Request rejected: Missing parameters field!");
		}

		IRequest request = new Request();
		Object controllerKey = null;

		request.setId(Short.valueOf(Short.parseShort(requestObject.getInt(ACTION_ID).toString())));
		controllerKey = Byte.valueOf(Byte.parseByte(requestObject.getInt(CONTROLLER_ID).toString()));

		request.setContent(requestObject.getGameObject(PARAM_ID));
		request.setSender(packet.getSender());
		request.setTransportType(packet.getTransportType());

		if (packet.isUdp()) {
			request.setAttribute("$FS_REQUEST_UDP_TIMESTAMP", requestObject.getLong("i"));
		}
		dispatchRequestToController(request, controllerKey);
	}

}