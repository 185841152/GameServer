package com.net.server.data;

public class GameDataWrapper
{
  private GameDataType typeId;
  private Object object;

  public GameDataWrapper(GameDataType typeId, Object object)
  {
    this.typeId = typeId;
    this.object = object;
  }

  public GameDataType getTypeId()
  {
    return this.typeId;
  }

  public Object getObject()
  {
    return this.object;
  }
}