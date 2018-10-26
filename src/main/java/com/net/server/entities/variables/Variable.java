package com.net.server.entities.variables;

import com.net.server.data.IGameArray;
import com.net.server.data.IGameObject;

public abstract interface Variable extends Cloneable {
	public abstract String getName();

	public abstract VariableType getType();

	public abstract Object getValue();

	public abstract Boolean getBoolValue();

	public abstract Integer getIntValue();

	public abstract Double getDoubleValue();

	public abstract String getStringValue();

	public abstract IGameObject getSFSObjectValue();

	public abstract IGameArray getSFSArrayValue();

	public abstract boolean isNull();

	public abstract IGameArray toSFSArray();
}