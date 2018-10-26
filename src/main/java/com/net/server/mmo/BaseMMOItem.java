package com.net.server.mmo;

import java.util.List;

import com.net.server.entities.User;

public abstract class BaseMMOItem implements IMMOItem {
	abstract P3D getLastPos();

	abstract void setLastPos(P3D paramP3D);

	abstract Vec3D getLastLocation();

	abstract void setLastLocation(Vec3D paramVec3D);

	abstract List<User> getLastProxyList();

	abstract void setLastProxyList(List<User> paramList);

	public abstract MMORoom getRoom();

	abstract void setRoom(MMORoom paramMMORoom);
}