package com.net.server.entities;

import com.net.engine.sessions.ISession;
import com.net.server.api.CreateRoomSettings;
import com.net.server.data.IGameArray;
import com.net.server.entities.managers.IRoomManager;
import com.net.server.entities.managers.IUserManager;
import com.net.server.exceptions.SFSCreateRoomException;
import com.net.server.exceptions.SFSLoginException;
import com.net.server.util.IResponseThrottler;

import java.util.Collection;
import java.util.List;

public abstract interface Zone {
	public abstract User login(ISession session, String userName,Integer userId) throws SFSLoginException;

	public void setUserReconnectionSeconds(int seconds);
	
	public abstract int getUserReconnectionSeconds();
	
	public void setMaxUserIdleTime(int seconds);

	public abstract int getMaxMembers();

	public abstract String getName();

	public abstract Object getProperty(Object key);

	public abstract void setProperty(Object key, Object value);

	public abstract boolean containsProperty(Object param);
	
	public abstract IUserManager getUserManager();

	public abstract IRoomManager getRoomManager();

	public abstract void removeUser(int userId);

	public abstract void removeUser(String userName);

	public abstract void removeUser(User user);

	public abstract Collection<User> getUsersInGroup(String groupId);

	public abstract Collection<User> getUserList();

	public abstract void removeAllUsers();

	public abstract void checkAndRemove(Room room);

	public abstract List<Room> getRoomList();

	public abstract List<Room> getRoomListFromGroup(String groupName);

	public abstract Room getRoomById(int roomId);

	public abstract Room getRoomByName(String roomName);

	public abstract void addRoom(Room paramRoom);

	public abstract void removeRoom(Room room);

	public abstract void removeRoom(int roomId);

	public abstract void removeRoom(String roomName);

	public abstract Room createRoom(CreateRoomSettings paramCreateRoomSettings) throws SFSCreateRoomException;

	public abstract Room createRoom(CreateRoomSettings paramCreateRoomSettings, User paramUser)
			throws SFSCreateRoomException;

	public abstract IGameArray getRoomListData();

	public abstract IGameArray getRoomListData(List<String> groupIds);

	public List<String> getDefaultGroups();

	public void setDefaultGroups(List<String> groupIDs);

	public abstract IResponseThrottler getUCountThrottler();

	public abstract void setUserCountChangeUpdateInterval(int interval);

	public void removeUserFromRoom(User user, Room room);

}
