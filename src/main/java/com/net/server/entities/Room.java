package com.net.server.entities;

import java.util.List;
import java.util.Set;

import com.net.server.data.IGameArray;
import com.net.server.entities.managers.IUserManager;
import com.net.server.entities.variables.RoomVariable;
import com.net.server.exceptions.SFSJoinRoomException;
import com.net.server.exceptions.SFSVariableException;

public abstract interface Room {

	public abstract Zone getZone();

	public abstract void setZone(Zone zone);

	public abstract int getId();
	
	public void setId(int id);

	public abstract String getGroupId();

	public abstract void setGroupId(String groupId);

	public abstract String getName();

	public abstract void setName(String name);

	public abstract String getPassword();

	public abstract void setPassword(String password);

	public abstract boolean isPasswordProtected();

	public abstract boolean isPublic();

	public abstract int getCapacity();

	public abstract void setCapacity(int maxUser, int maxSpectators);

	public abstract int getMaxUsers();

	public abstract void setMaxUsers(int count);

	public abstract int getMaxSpectators();

	public abstract void setMaxSpectators(int paramInt);

	public abstract int getMaxRoomVariablesAllowed();

	public abstract void setMaxRoomVariablesAllowed(int paramInt);

	public abstract User getOwner();

	public abstract void setOwner(User paramUser);

	public abstract RoomSize getSize();

	public abstract IUserManager getUserManager();

	public abstract void setUserManager(IUserManager paramIUserManager);

	public abstract boolean isDynamic();

	public abstract boolean isGame();

	public abstract boolean isHidden();

	public abstract void setDynamic(boolean paramBoolean);

	public abstract void setHidden(boolean paramBoolean);

	public abstract void setFlags(Set<GameRoomSettings> paramSet);

	public abstract void setFlag(GameRoomSettings paramSFSRoomSettings, boolean paramBoolean);

	public abstract boolean isFlagSet(GameRoomSettings paramSFSRoomSettings);

	public abstract GameRoomRemoveMode getAutoRemoveMode();

	public abstract void setAutoRemoveMode(GameRoomRemoveMode paramSFSRoomRemoveMode);

	public abstract boolean isEmpty();

	public abstract boolean isFull();

	public abstract boolean isActive();

	public abstract void setActive(boolean paramBoolean);

	public abstract RoomVariable getVariable(String paramString);

	public abstract List<RoomVariable> getVariables();

	public abstract void setVariable(RoomVariable paramRoomVariable, boolean paramBoolean) throws SFSVariableException;

	public abstract void setVariable(RoomVariable paramRoomVariable) throws SFSVariableException;

	public abstract void setVariables(List<RoomVariable> paramList, boolean paramBoolean);

	public abstract void setVariables(List<RoomVariable> paramList);

	public abstract List<RoomVariable> getVariablesCreatedByUser(User paramUser);

	public abstract List<RoomVariable> removeVariablesCreatedByUser(User paramUser);

	public abstract void removeVariable(String paramString);

	public abstract boolean containsVariable(String paramString);

	public abstract int getVariablesCount();

	public abstract Object getProperty(Object paramObject);

	public abstract void setProperty(Object paramObject1, Object paramObject2);

	public abstract boolean containsProperty(Object paramObject);

	public abstract void removeProperty(Object paramObject);

	public abstract User getUserById(int paramInt);

	public abstract User getUserByName(String paramString);

	public abstract User getUserByPlayerId(int paramInt);

	public abstract List<User> getUserList();

	public abstract List<User> getPlayersList();

	public abstract List<User> getSpectatorsList();

	public abstract IGameArray getUserListData();

	public abstract IGameArray getRoomVariablesData(boolean paramBoolean);

	public abstract void addUser(User paramUser, boolean paramBoolean) throws SFSJoinRoomException;

	public abstract void addUser(User paramUser) throws SFSJoinRoomException;

	public abstract void removeUser(User paramUser);

	public abstract boolean containsUser(User paramUser);

	public abstract boolean containsUser(String paramString);

	public abstract IGameArray toSFSArray(boolean paramBoolean);

	public abstract boolean isUseWordsFilter();

	public abstract void setUseWordsFilter(boolean paramBoolean);

	public abstract long getLifeTime();

	public abstract void destroy();

}