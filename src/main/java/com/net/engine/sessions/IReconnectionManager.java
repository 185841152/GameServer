package com.net.engine.sessions;

import com.net.engine.exceptions.SessionReconnectionException;

public abstract interface IReconnectionManager
{
  public abstract ISession getReconnectableSession(String paramString);

  public abstract ISession reconnectSession(ISession paramISession, String paramString)
    throws SessionReconnectionException;

  public abstract void onSessionLost(ISession paramISession);

  public abstract ISessionManager getSessionManager();
}