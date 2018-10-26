package com.net.engine.data;

import java.nio.channels.SelectableChannel;

import com.net.engine.config.SocketConfig;

public class BindableSocket extends SocketConfig
{
  protected SelectableChannel channel;

  public BindableSocket(SelectableChannel channel, String address, int port, TransportType type)
  {
    super(address, port, type);
    this.channel = channel;
  }

  public SelectableChannel getChannel()
  {
    return this.channel;
  }
}