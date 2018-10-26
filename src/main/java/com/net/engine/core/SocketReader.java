package com.net.engine.core;

import com.net.engine.data.IPacket;
import com.net.engine.io.IRequest;
import com.net.engine.io.IResponse;
import com.net.engine.io.Request;
import com.net.engine.io.protocols.ProtocolType;
import com.net.server.controllers.SystemRequest;
import com.net.server.data.IGameObject;

public class SocketReader extends ISocketReader {
	private static final String ACTION_ID = "a";
	private static final String PARAM_ID = "p";


	public void onPacketRead(IPacket packet) {
		if (packet == null) {
			throw new IllegalStateException("Protocol Codec didn't expect a null packet!");
		}
		ProtocolType type = packet.getProtocolType();
		IGameObject requestObject = (IGameObject) packet.getData();

		dispatchRequest(requestObject, packet, type == ProtocolType.BINARY ? true : false);
	}

	private void dispatchRequest(IGameObject requestObject, IPacket packet, boolean isBinary) {
		if (requestObject.isNull(ACTION_ID)) {
			throw new IllegalStateException("没有指定ActonId");
		}
		IRequest request = new Request();
		Object controllerKey = null;
		if (isBinary) {
			request.setId(requestObject.getShort(ACTION_ID));
			SystemRequest systemRequest=SystemRequest.fromId(requestObject.getShort(ACTION_ID));
			controllerKey = systemRequest.getType();
		} else {
			request.setId(Short.valueOf(requestObject.getInt(ACTION_ID).shortValue()));
			SystemRequest systemRequest=SystemRequest.fromId(Short.valueOf(requestObject.getInt(ACTION_ID).shortValue()));
			controllerKey = systemRequest.getType();
		}
		request.setContent(requestObject.getGameObject(PARAM_ID));
		request.setSender(packet.getSender());
		request.setTransportType(packet.getTransportType());

		dispatchRequestToController(request, controllerKey);
	}

	public void onPacketWrite(IResponse paramIResponse) {
		
	}


}