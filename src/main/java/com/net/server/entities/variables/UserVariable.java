package com.net.server.entities.variables;

public abstract interface UserVariable extends Variable {
	public abstract void setHidden(boolean paramBoolean);

	public abstract boolean isHidden();

	public abstract void setNull();
}