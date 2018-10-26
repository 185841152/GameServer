package com.net.server.mmo;

import java.util.List;

import com.net.server.entities.User;

public abstract interface IMMOUpdateManager {
	public abstract void addUserToUpdate(User paramUser);

	public abstract void addBatchToUpdate(List<User> paramList);

	public abstract void addItemToUpdate(BaseMMOItem paramBaseMMOItem);

	public abstract int getUpdateThreshold();

	public abstract void setUpdateThreshold(int paramInt);

	public abstract void destroy();
}