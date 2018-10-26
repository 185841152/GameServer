package com.net.business.extensions.handler.room;

import com.net.business.extensions.core.BaseClientRequestHandler;
import com.net.business.extensions.handler.Constants;
import com.net.server.api.CreateRoomSettings;
import com.net.server.controllers.SystemRequest;
import com.net.server.data.GameObject;
import com.net.server.data.IGameObject;
import com.net.server.entities.GameRoomRemoveMode;
import com.net.server.entities.Room;
import com.net.server.entities.User;
import com.net.server.exceptions.GameException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateRoomHandler extends BaseClientRequestHandler {
	private Logger logger=LoggerFactory.getLogger(CreateRoomHandler.class);
	@Override
	public void handleClientRequest(User user, IGameObject params) throws Exception {
		IGameObject result = GameObject.newInstance();
		try {
			CreateRoomSettings createRoomSettings = new CreateRoomSettings();
			createRoomSettings.setAutoRemoveMode(GameRoomRemoveMode.NEVER_REMOVE);
			createRoomSettings.setGame(true);
			createRoomSettings.setMaxUsers(5);
			createRoomSettings.setName("好友房");
			createRoomSettings.setDynamic(true);
			Room room = getParentExtension().getApi().createRoom(user.getZone(), createRoomSettings, user, false, null,false, false);
			//房间状态
			room.setProperty("status", Constants.RoomStatus.Wait.getStatus());
			//加入房间
			getParentExtension().getApi().joinRoom(user, room);
		} catch (GameException e) {
			this.logger.error("创建房间出错:{},{}",user.getId(),e);
			result.putShort("ec",e.getErrorData().getCode().getId());
			sendResponse(SystemRequest.CreateRoom.getId(), result, user);
		}
	}

}
