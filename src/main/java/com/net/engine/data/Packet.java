package com.net.engine.data;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.net.engine.io.protocols.ProtocolType;
import com.net.engine.sessions.ISession;

public class Packet implements IPacket {
	protected long creationTime;
	protected Object data;
	protected String ownerNode;
	protected MessagePriority priority;
	protected ISession sender;
	protected TransportType transportType;
	protected ProtocolType protocolType;
	protected int originalSize = -1;
	protected ConcurrentMap<String, Object> attributes;
	protected ISession recipients;
	protected byte[] fragmentBuffer;

	public Packet() {
		this.creationTime = System.nanoTime();
		this.priority = MessagePriority.NORMAL;
		this.transportType = TransportType.TCP;
	}

	public Object getAttribute(String key) {
		if (this.attributes == null) {
			return null;
		}
		return this.attributes.get(key);
	}

	public void setAttribute(String key, Object attr) {
		if (this.attributes == null) {
			this.attributes = new ConcurrentHashMap<String, Object>();
		}
		this.attributes.put(key, attr);
	}

	public long getCreationTime() {
		return this.creationTime;
	}

	public void setCreationTime(long creationTime) {
		this.creationTime = creationTime;
	}

	public Object getData() {
		return this.data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public String getOwnerNode() {
		return this.ownerNode;
	}

	public void setOwnerNode(String ownerNode) {
		this.ownerNode = ownerNode;
	}

	public MessagePriority getPriority() {
		return this.priority;
	}

	public void setPriority(MessagePriority priority) {
		this.priority = priority;
	}

	public ISession getSender() {
		return this.sender;
	}

	public void setSender(ISession sender) {
		this.sender = sender;
	}

	public TransportType getTransportType() {
		return this.transportType;
	}

	public void setTransportType(TransportType transportType) {
		this.transportType = transportType;
	}
	

	public ProtocolType getProtocolType() {
		return protocolType;
	}

	public void setProtocolType(ProtocolType protocolType) {
		this.protocolType = protocolType;
	}

	public ISession getRecipients() {
		return this.recipients;
	}

	public void setRecipients(ISession recipients) {
		this.recipients = recipients;
	}

	public boolean isTcp() {
		return this.transportType == TransportType.TCP;
	}

	public boolean isUdp() {
		return this.transportType == TransportType.UDP;
	}
	
	public boolean isBinary(){
		return this.protocolType==ProtocolType.BINARY;
	}

	public boolean isFragmented() {
		return this.fragmentBuffer != null;
	}

	public int getOriginalSize() {
		return this.originalSize;
	}

	public void setOriginalSize(int originalSize) {
		if (this.originalSize == -1)
			this.originalSize = originalSize;
	}

	public byte[] getFragmentBuffer() {
		return this.fragmentBuffer;
	}

	public void setFragmentBuffer(byte[] bb) {
		this.fragmentBuffer = bb;
	}

	public String toString() {
		return String.format("{ Packet: %s, data: %s, Pri: %s }",
				new Object[] { this.transportType, this.data.getClass().getName(), this.priority });
	}

	public IPacket clone() {
		IPacket newPacket = new Packet();

		newPacket.setCreationTime(getCreationTime());
		newPacket.setData(getData());
		newPacket.setOriginalSize(getOriginalSize());
		newPacket.setOwnerNode(getOwnerNode());
		newPacket.setPriority(getPriority());
		newPacket.setRecipients(null);
		newPacket.setSender(getSender());
		newPacket.setTransportType(getTransportType());
		newPacket.setProtocolType(getProtocolType());

		return newPacket;
	}

	@SuppressWarnings("unused")
	private byte[] cloneData(Object data) {
		if ((data instanceof byte[])) {
			byte[] newData = new byte[((byte[]) data).length];
			System.arraycopy(data, 0, newData, 0, newData.length);
			return newData;
		}

		return null;
	}

}