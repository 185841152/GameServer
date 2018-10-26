package com.net.server.mmo;

import java.util.List;

import com.net.server.data.IGameArray;

public abstract interface IMMOItem {
	public abstract int getId();

	public abstract IMMOItemVariable getVariable(String paramString);

	public abstract List<IMMOItemVariable> getVariables();

	public abstract void setVariable(IMMOItemVariable paramIMMOItemVariable);

	public abstract void setVariables(List<IMMOItemVariable> paramList);

	public abstract void removeVariable(String paramString);

	public abstract IGameArray toSFSArray();
}