package com.net.engine.io;

import java.util.List;

import com.net.engine.data.TransportType;
import com.net.engine.sessions.ISession;

public abstract interface IResponse extends IEngineMessage {
	public abstract TransportType getTransportType();

	public abstract void setTransportType(TransportType paramTransportType);

	public abstract ISession getRecipients();

	public abstract void setRecipients(ISession paramISession);
	
	public List<Integer> getUserIds();

	public void setUserIds(List<Integer> userIds);
	
	public void setUserId(Integer userId);

	public abstract boolean isTCP();

	public abstract boolean isUDP();

	public abstract void write();

	public abstract void write(int paramInt);
}