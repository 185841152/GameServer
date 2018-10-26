package com.net.server.mmo;

import java.util.List;

import com.net.server.entities.User;

public abstract interface IMMOItemManager {
	public abstract void setItem(BaseMMOItem paramBaseMMOItem, Vec3D paramVec3D);

	public abstract void removeItem(BaseMMOItem paramBaseMMOItem);

	public abstract List<BaseMMOItem> getItemList(User paramUser);

	public abstract List<BaseMMOItem> getItemList(User paramUser, Vec3D paramVec3D);

	public abstract List<BaseMMOItem> getItemList(Vec3D paramVec3D);

	public abstract List<BaseMMOItem> getItemList(Vec3D paramVec3D1, Vec3D paramVec3D2);

	public abstract int getSize();
}