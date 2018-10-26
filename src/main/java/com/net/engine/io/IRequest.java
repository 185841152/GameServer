package com.net.engine.io;

import com.net.engine.data.MessagePriority;
import com.net.engine.data.TransportType;
import com.net.engine.sessions.ISession;

public abstract interface IRequest extends IEngineMessage {
	public abstract TransportType getTransportType();

	public abstract void setTransportType(TransportType paramTransportType);

	public abstract ISession getSender();

	public abstract void setSender(ISession paramISession);

	public abstract MessagePriority getPriority();

	public abstract void setPriority(MessagePriority paramMessagePriority);

	public abstract long getTimeStamp();

	public abstract void setTimeStamp(long paramLong);

	public abstract boolean isTcp();

	public abstract boolean isUdp();
}