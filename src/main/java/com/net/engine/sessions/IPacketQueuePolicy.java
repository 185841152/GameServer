package com.net.engine.sessions;

import com.net.engine.data.IPacket;
import com.net.engine.exceptions.PacketQueueWarning;

public abstract interface IPacketQueuePolicy
{
  public abstract void applyPolicy(IPacketQueue paramIPacketQueue, IPacket paramIPacket)
    throws PacketQueueWarning;
}