package com.net.engine.sessions;

import com.net.engine.data.IPacket;
import com.net.engine.exceptions.MessageQueueFullException;

public abstract interface IPacketQueue {
	public abstract IPacket peek();

	public abstract IPacket take();

	public abstract boolean isEmpty();

	public abstract boolean isFull();

	public abstract int getSize();

	public abstract int getMaxSize();

	public abstract void setMaxSize(int paramInt);

	public abstract float getPercentageUsed();

	public abstract void clear();

	public abstract void put(IPacket paramIPacket) throws MessageQueueFullException;

	public abstract IPacketQueuePolicy getPacketQueuePolicy();

	public abstract void setPacketQueuePolicy(IPacketQueuePolicy paramIPacketQueuePolicy);
}