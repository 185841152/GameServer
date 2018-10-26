package com.net.server.mmo;

import java.util.List;

import com.net.server.entities.User;

public abstract interface IProximityManager {
	public abstract void addUser(User paramUser);

	public abstract void updateUser(User paramUser);

	public abstract void removeUser(User paramUser);

	public abstract List<User> getProximityList(User paramUser);

	public abstract List<User> getProximityList(User paramUser, Vec3D paramVec3D);

	public abstract List<User> getProximityList(Vec3D paramVec3D);

	public abstract List<User> getProximityList(Vec3D paramVec3D1, Vec3D paramVec3D2);

	public abstract int getSize();

	public abstract Vec3D getSectorSize();

	public abstract Vec3D getDefaultAOI();
}