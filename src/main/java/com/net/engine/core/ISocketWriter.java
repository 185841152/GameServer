package com.net.engine.core;

import com.net.engine.data.IPacket;
import com.net.engine.io.IResponse;
import com.net.engine.sessions.ISession;

public abstract interface ISocketWriter {

	public abstract void continueWriteOp(ISession paramISession);
	
	public void onPacketWrite(IResponse response);

	public abstract void enqueuePacket(IPacket paramIPacket);

	public abstract long getDroppedPacketsCount();

	public abstract long getWrittenBytes();

	public abstract long getWrittenPackets();

	public abstract int getQueueSize();

	public abstract int getThreadPoolSize();
}