package com.net.server.exceptions;

public class SessionReconnectionException extends Exception
{
  private static final long serialVersionUID = 1L;

  public SessionReconnectionException(String message)
  {
    super(message);
  }
}