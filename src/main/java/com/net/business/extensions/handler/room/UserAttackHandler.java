package com.net.business.extensions.handler.room;

import com.net.business.extensions.core.BaseClientRequestHandler;
import com.net.server.controllers.SystemRequest;
import com.net.server.data.GameObject;
import com.net.server.data.IGameObject;
import com.net.server.entities.Room;
import com.net.server.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserAttackHandler extends BaseClientRequestHandler {
	private Logger logger=LoggerFactory.getLogger(UserAttackHandler.class);
	@Override
	public void handleClientRequest(User user, IGameObject params) throws Exception {
		IGameObject result = GameObject.newInstance();
		try {
			Room room=user.getLastJoinedRoom();
			if(room!=null){
				sendResponse(SystemRequest.OnUserAttack.getId(),params,room.getUserList());
			}
		} catch (Exception e) {
			this.logger.error("创建房间出错:{},{}",user.getId(),e);
			result.putShort("ec",Short.valueOf("500"));
			sendResponse(SystemRequest.OnUserAttack.getId(), result, user);
		}
	}

}
