package com.net.engine.websocket;

import com.net.engine.service.BaseCoreService;
import com.net.engine.websocket.boot.WebSocketBoot;

public class WebSocketService extends BaseCoreService
{
  private volatile boolean inited = false;
  private final WebSocketStats webSocketStats;
  private final WebSocketProtocolCodec protocolCodec;
  private final boolean isActive;

  public WebSocketService()
  {
    this.isActive = true;

    this.webSocketStats = new WebSocketStats();
    this.protocolCodec = new WebSocketProtocolCodec(this.webSocketStats);
  }

  public void init(Object o)
  {
    if (this.inited) {
      throw new IllegalArgumentException("Service is already initialized. Destroy it first!");
    }
    this.inited = true;
    new WebSocketBoot((WebSocketConfig)o, this.protocolCodec);
  }

  public void destroy(Object o)
  {
    super.destroy(o);
  }

  public boolean isActive()
  {
    return this.isActive;
  }

  public WebSocketStats getWebSocketStats()
  {
    return this.webSocketStats;
  }

  public WebSocketProtocolCodec getProtocolCodec()
  {
    return this.protocolCodec;
  }
}