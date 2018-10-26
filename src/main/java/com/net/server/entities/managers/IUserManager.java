package com.net.server.entities.managers;

import java.util.Collection;
import java.util.List;

import com.net.server.core.ICoreService;
import com.net.server.entities.Room;
import com.net.server.entities.User;

public abstract interface IUserManager extends ICoreService {
	public abstract User getUserByName(String userName);

	public abstract User getUserById(int userId);

	public abstract List<User> getAllUsers();

	public abstract Collection<User> getDirectUserList();

	public abstract void addUser(User user);

	public abstract void removeUser(User user);

	public abstract void removeUser(String userName);

	public abstract void removeUser(int userId);

	public abstract void disconnectUser(User user);

	public abstract void disconnectUser(String userName);

	public abstract void disconnectUser(int userId);

	public abstract boolean containsId(int userId);

	public abstract boolean containsName(String userName);

	public abstract boolean containsUser(User user);

	public abstract Room getOwnerRoom();

	public abstract void setOwnerRoom(Room paramRoom);

	public abstract int getUserCount();

	public abstract int getHighestCCU();
}