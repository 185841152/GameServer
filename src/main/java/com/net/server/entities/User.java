package com.net.server.entities;

import java.util.List;
import java.util.Map;

import com.net.server.data.IGameArray;
import com.net.server.entities.variables.UserVariable;
import com.net.server.exceptions.SFSVariableException;
import com.net.server.mmo.BaseMMOItem;
import com.net.server.mmo.MMORoom;

public abstract interface User {
	
	public abstract int getId();

	public abstract String getName();

	public abstract void setName(String paramString);

	public abstract Zone getZone();

	public abstract void setZone(Zone paramZone);

	public abstract long getLoginTime();

	public abstract void setLastLoginTime(long paramLong);

	public abstract Room getLastJoinedRoom();

	public abstract List<Room> getJoinedRooms();

	public abstract void addJoinedRoom(Room paramRoom);

	public abstract void removeJoinedRoom(Room paramRoom);

	public abstract boolean isJoinedInRoom(Room paramRoom);

	public abstract void addCreatedRoom(Room paramRoom);

	public abstract void removeCreatedRoom(Room paramRoom);

	public abstract List<Room> getCreatedRooms();

	public abstract void subscribeGroup(String paramString);

	public abstract void unsubscribeGroup(String paramString);

	public abstract List<String> getSubscribedGroups();

	public abstract boolean isSubscribedToGroup(String paramString);

	public abstract int getPlayerId();

	public abstract int getPlayerId(Room paramRoom);

	public abstract void setPlayerId(int paramInt, Room paramRoom);

	public abstract Map<Room, Integer> getPlayerIds();

	public abstract boolean isPlayer();

	public abstract boolean isSpectator();

	public abstract boolean isPlayer(Room paramRoom);

	public abstract boolean isSpectator(Room paramRoom);

	public abstract boolean isJoining();

	public abstract void setJoining(boolean paramBoolean);

	public abstract int getMaxAllowedVariables();

	public abstract void setMaxAllowedVariables(int paramInt);

	public abstract Object getProperty(Object paramObject);

	public abstract void setProperty(Object paramObject1, Object paramObject2);

	public abstract boolean containsProperty(Object paramObject);

	public abstract void removeProperty(Object paramObject);

	public abstract int getOwnedRoomsCount();

	public abstract boolean isBeingKicked();

	public abstract void setBeingKicked(boolean paramBoolean);

	public abstract short getPrivilegeId();

	public abstract void setPrivilegeId(short paramShort);

	public abstract UserVariable getVariable(String paramString);

	public abstract List<UserVariable> getVariables();

	public abstract void setVariable(UserVariable paramUserVariable) throws SFSVariableException;

	public abstract void setVariables(List<UserVariable> paramList) throws SFSVariableException;

	public abstract void removeVariable(String paramString);

	public abstract boolean containsVariable(String paramString);

	public abstract int getVariablesCount();

	public abstract IGameArray getUserVariablesData();

	public abstract IGameArray toGameArray(Room paramRoom);

	public abstract IGameArray toGameArray();

	public abstract String getDump();

	public abstract List<User> getLastProxyList();

	public abstract void setLastProxyList(List<User> paramList);

	public abstract List<BaseMMOItem> getLastMMOItemsList();

	public abstract void setLastMMOItemsList(List<BaseMMOItem> paramList);

	public abstract MMORoom getCurrentMMORoom();
	
	public int getSessionId();

	public void setSessionId(int sessionId);
	
	public void setConnected(boolean flag);
	
	public boolean isConnected();
	
	public String getIp();

	public void setIp(String ip);
	
	public int getStatus();

	public void setStatus(int status);
}