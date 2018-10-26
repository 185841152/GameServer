package com.net.engine.io;

import com.net.engine.data.IPacket;

public abstract interface IProtocolCodec
{
  public abstract void onPacketRead(IPacket paramIPacket);

  public abstract void onPacketWrite(IResponse paramIResponse);

}