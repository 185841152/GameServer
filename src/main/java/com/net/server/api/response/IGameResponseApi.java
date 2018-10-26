package com.net.server.api.response;

import java.util.List;

import com.net.server.controllers.SystemRequest;
import com.net.server.data.IGameObject;
import com.net.server.entities.Room;
import com.net.server.entities.User;
import com.net.server.exceptions.ErrorData;
import com.net.server.exceptions.GameException;

public abstract interface IGameResponseApi {

	public abstract void sendExtResponse(Object cmdName, IGameObject params, List<User> users, Room room,boolean sendUdp);

	public void notifyRequestError(ErrorData errData, User recipient, SystemRequest requestType);

	public void notifyRequestError(GameException err, User recipient, SystemRequest requestType);

	public void notifyJoinRoomSuccess(User recipient, Room joinedRoom);

	public void notifyUserEnterRoom(User user, Room room);

	public void notifyUserExitRoom(User user, Room room, boolean sendToEveryOne);
	
	public void notifyUserOnLineRoom(User user, Room room);
	
	public void notifyRoomRemoved(Room room);
	
}