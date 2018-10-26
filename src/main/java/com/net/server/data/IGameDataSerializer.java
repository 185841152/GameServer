package com.net.server.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public abstract interface IGameDataSerializer {
	public abstract byte[] object2binary(IGameObject paramIGameObject);

	public abstract byte[] array2binary(IGameArray paramIGameArray);

	public abstract IGameObject binary2object(byte[] paramArrayOfByte);

	public abstract IGameArray binary2array(byte[] paramArrayOfByte);

	public abstract String object2json(Map<String, Object> paramMap);

	public abstract String array2json(List<Object> paramList);

	public abstract IGameObject json2object(String paramString);

	public abstract IGameArray json2array(String paramString);

	public abstract IGameObject pojo2game(Object paramObject);

	public abstract Object game2pojo(IGameObject paramIGameObject);

	public abstract GameObject resultSet2object(ResultSet paramResultSet) throws SQLException;

	public abstract GameArray resultSet2array(ResultSet paramResultSet) throws SQLException;
}