package com.net.engine.io;


import com.net.engine.data.MessagePriority;
import com.net.engine.data.TransportType;
import com.net.engine.sessions.ISession;

public final class Request extends AbstractEngineMessage implements IRequest {
	private ISession sender;
	private TransportType type;
	private MessagePriority priority;
	private long timeStamp;

	public Request() {
		this.type = TransportType.TCP;
		this.priority = MessagePriority.NORMAL;
		this.timeStamp = System.nanoTime();
	}

	public ISession getSender() {
		return this.sender;
	}

	public TransportType getTransportType() {
		return this.type;
	}

	public void setSender(ISession session) {
		this.sender = session;
	}

	public void setTransportType(TransportType type) {
		this.type = type;
	}

	public MessagePriority getPriority() {
		return this.priority;
	}

	public void setPriority(MessagePriority priority) {
		this.priority = priority;
	}

	public long getTimeStamp() {
		return this.timeStamp;
	}

	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	public boolean isTcp() {
		return this.type == TransportType.TCP;
	}

	public boolean isUdp() {
		return this.type == TransportType.UDP;
	}

	public String toString() {
		return String.format("[Req Type: %s, Sender: %s]", new Object[] { this.type, this.sender });
	}
}