package com.net.server.entities.managers;

import java.util.List;

import com.net.server.api.CreateRoomSettings;
import com.net.server.core.ICoreService;
import com.net.server.entities.Room;
import com.net.server.entities.User;
import com.net.server.entities.Zone;
import com.net.server.exceptions.SFSCreateRoomException;
import com.net.server.exceptions.SFSRoomException;


public abstract interface IRoomManager extends ICoreService {
	public abstract void addRoom(Room paramRoom);

	public abstract Room createRoom(CreateRoomSettings paramCreateRoomSettings) throws SFSCreateRoomException;

	public abstract Room createRoom(CreateRoomSettings paramCreateRoomSettings, User paramUser)
			throws SFSCreateRoomException;

	public abstract List<String> getGroups();

	public abstract void addGroup(String paramString);

	public abstract void removeGroup(String paramString);

	public abstract boolean containsGroup(String paramString);

	public abstract boolean containsRoom(int paramInt);

	public abstract boolean containsRoom(String paramString);

	public abstract boolean containsRoom(Room paramRoom);

	public abstract boolean containsRoom(int paramInt, String paramString);

	public abstract boolean containsRoom(String paramString1, String paramString2);

	public abstract boolean containsRoom(Room paramRoom, String paramString);

	public abstract Room getRoomById(int paramInt);

	public abstract Room getRoomByName(String paramString);

	public abstract List<Room> getRoomList();

	public abstract List<Room> getRoomListFromGroup(String paramString);

	public abstract int getGameRoomCount();

	public abstract int getTotalRoomCount();

	public abstract void checkAndRemove(Room paramRoom);

	public abstract void removeRoom(Room paramRoom);

	public abstract void removeRoom(int paramInt);

	public abstract void removeRoom(String paramString);

	public abstract Zone getOwnerZone();

	public abstract void setOwnerZone(Zone paramZone);

	public abstract void removeUser(User paramUser);

	public abstract void removeUser(User paramUser, Room paramRoom);

	public abstract void changeRoomName(Room paramRoom, String paramString) throws SFSRoomException;

	public abstract void changeRoomPasswordState(Room paramRoom, String paramString);

	public abstract void changeRoomCapacity(Room paramRoom, int paramInt1, int paramInt2);
}