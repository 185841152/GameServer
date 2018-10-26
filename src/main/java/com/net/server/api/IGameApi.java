package com.net.server.api;

import com.net.engine.sessions.ISession;
import com.net.server.api.response.IGameResponseApi;
import com.net.server.data.IGameObject;
import com.net.server.entities.Room;
import com.net.server.entities.User;
import com.net.server.entities.Zone;
import com.net.server.entities.variables.RoomVariable;
import com.net.server.entities.variables.UserVariable;
import com.net.server.exceptions.SFSCreateRoomException;
import com.net.server.exceptions.SFSJoinRoomException;

import java.util.List;

public abstract interface IGameApi {

	public abstract IGameResponseApi getResponseAPI();

	public abstract void sendExtensionResponse(Object cmd, IGameObject params, List<User> users, Room room,boolean isudp);

	public abstract void sendExtensionResponse(Object cmd, IGameObject params, User user, Room room, boolean isudp);

	public abstract boolean checkSecurePassword(ISession session, String originalPass, String encryptedPass);

	public abstract User login(ISession session, String name,Integer userId,String ip,String token, int uid, String zoneName);

	public abstract User login(ISession session, String name,Integer userId,String ip,String token, int uid, String zoneName,boolean forceLogout);

	public abstract Room createRoom(Zone zone, CreateRoomSettings params, User owner) throws SFSCreateRoomException;

	public abstract Room createRoom(Zone zone, CreateRoomSettings params, User owner, boolean joinIt, Room roomToLeave)throws SFSCreateRoomException;

	public abstract Room createRoom(Zone zone, CreateRoomSettings params, User owner, boolean joinIt, Room roomToLeave,boolean fireClientEvent, boolean fireServerEvent) throws SFSCreateRoomException;

	public abstract User getUserById(int userId);

	public abstract User getUserByName(String userName);

	public abstract void joinRoom(User user, Room room) throws SFSJoinRoomException;

	public abstract void joinRoom(User user, Room roomToJoin, String password, boolean asSpectator, Room roomToLeave) throws SFSJoinRoomException;

	public abstract void joinRoom(User user, Room roomToJoin, String password, boolean asSpectator, Room roomToLeave,boolean fireClientEvent, boolean fireServerEvent) throws SFSJoinRoomException;

	public abstract void leaveRoom(User user, Room room);

	public abstract void leaveRoom(User user, Room room, boolean fireClientEvent, boolean fireServerEvent);

	public abstract void removeRoom(Room room);

	public abstract void removeRoom(Room room, boolean fireClientEvent, boolean fireServerEvent);

	public abstract void setRoomVariables(User user, Room targetRoom, List<RoomVariable> variables);

	public abstract void setRoomVariables(User user, Room targetRoom, List<RoomVariable> variables, boolean fireClientEvent,boolean fireServerEvent, boolean overrideOwnership);

	public abstract void setUserVariables(User owner, List<UserVariable> variables);

	public abstract void setUserVariables(User owner, List<UserVariable> variables, boolean fireClientEvent,boolean fireServerEvent);

}