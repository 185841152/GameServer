package com.net.business.extensions.handler.room;


import com.net.business.extensions.core.BaseClientRequestHandler;
import com.net.server.controllers.SystemRequest;
import com.net.server.data.GameObject;
import com.net.server.data.IGameObject;
import com.net.server.entities.Room;
import com.net.server.entities.User;
import com.net.server.exceptions.ErrorCode;
import com.net.server.exceptions.ErrorData;
import com.net.server.exceptions.GameException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JoinRoomHandler extends BaseClientRequestHandler {
	private Logger logger=LoggerFactory.getLogger(JoinRoomHandler.class);
	@Override
	public void handleClientRequest(User user, IGameObject params) throws Exception {
		IGameObject result=GameObject.newInstance();
		try {

			int roomId=params.getInt("r");
			Room room=user.getZone().getRoomById(roomId);
			if (room==null) {
				ErrorData errData = new ErrorData(ErrorCode.ROOM_NOT_EXITS);
				GameException err = new GameException("房间不存在", errData);
				throw err;
			}
			//判断用户是否已经加入房间
			if (room.containsUser(user)) {
				//加入房间
				getParentExtension().getApi().getResponseAPI().notifyJoinRoomSuccess(user, room);
				return;
			}
//			int status=(int) room.getProperty("status");
//			if (status== Constants.RoomStatus.InGame.getStatus()) {
//				ErrorData errData = new ErrorData(ErrorCode.ROOM_NOT_EXITS);
//				GameException err = new GameException("游戏已经开始", errData);
//				throw err;
//			}
			//加入房间
			getParentExtension().getApi().joinRoom(user, room);
		} catch (GameException e) {
			this.logger.error("加入房间出错:{},{}",user.getId(),e);
			result.putShort("ec", e.getErrorData().getCode().getId());
			sendResponse(SystemRequest.JoinRoom.getId(), result, user);
		}
	}
	
}
