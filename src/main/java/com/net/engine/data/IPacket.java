package com.net.engine.data;

import com.net.engine.io.protocols.ProtocolType;
import com.net.engine.sessions.ISession;

public abstract interface IPacket {
	public abstract Object getData();

	public abstract void setData(Object paramObject);

	public abstract TransportType getTransportType();

	public abstract ProtocolType getProtocolType();
	
	public abstract void setTransportType(TransportType paramTransportType);
	
	public abstract void setProtocolType(ProtocolType protocolType);

	public abstract MessagePriority getPriority();

	public abstract void setPriority(MessagePriority paramMessagePriority);

	public abstract ISession getRecipients();

	public abstract void setRecipients(ISession paramCollection);

	public abstract byte[] getFragmentBuffer();

	public abstract void setFragmentBuffer(byte[] paramArrayOfByte);

	public abstract ISession getSender();

	public abstract void setSender(ISession paramISession);

	public abstract Object getAttribute(String paramString);

	public abstract void setAttribute(String paramString, Object paramObject);

	public abstract String getOwnerNode();

	public abstract void setOwnerNode(String paramString);

	public abstract long getCreationTime();

	public abstract void setCreationTime(long paramLong);

	public abstract int getOriginalSize();

	public abstract void setOriginalSize(int paramInt);
	
	public abstract boolean isBinary();

	public abstract boolean isTcp();

	public abstract boolean isUdp();

	public abstract boolean isFragmented();

	public abstract IPacket clone();
}