package com.net.server.exceptions;

public class JoinRoomException extends GameException
{
  private static final long serialVersionUID = 6384101728401558209L;

  public JoinRoomException()
  {
  }

  public JoinRoomException(String message)
  {
    super(message);
  }

  public JoinRoomException(String message, ErrorData data)
  {
    super(message, data);
  }
}