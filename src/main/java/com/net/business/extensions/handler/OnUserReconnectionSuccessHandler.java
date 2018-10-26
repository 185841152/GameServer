package com.net.business.extensions.handler;

import com.net.business.entity.GameData;
import com.net.business.extensions.core.BaseClientRequestHandler;
import com.net.server.controllers.SystemRequest;
import com.net.server.data.GameObject;
import com.net.server.data.IGameObject;
import com.net.server.entities.Room;
import com.net.server.entities.User;

import java.util.List;

public class OnUserReconnectionSuccessHandler extends BaseClientRequestHandler {

	@Override
	public void handleClientRequest(User user, IGameObject params) throws Exception {
		user.setConnected(true);
		Room room=user.getLastJoinedRoom();
		if (room!=null) {
			List<User> recipients = room.getUserList();
			recipients.remove(user);
		    if (recipients.size() > 0){
		    	IGameObject resObj=GameObject.newInstance();
				resObj.putInt("idx", (int)user.getProperty("idx"));
				sendResponse(SystemRequest.OnUserOnline.getId(), resObj, recipients);
		    }
			IGameObject resObj = GameObject.newInstance();
		    int status = (int) room.getProperty("status");
		    if(status == Constants.RoomStatus.InGame.getStatus()){
				GameData gameData = (GameData) room.getProperty("data");
				resObj.putGameObject("data",gameData.toGameObject());
			}
			resObj.putInt("r", room.getId());
			resObj.putInt("idx", (int)user.getProperty("idx"));
			resObj.putInt("status", status);
			if (room.getOwner()!=null) {
				resObj.putInt("ro", (int)room.getOwner().getProperty("idx"));
			}
			resObj.putGameArray("ul",room.getUserListData());
			sendResponse(SystemRequest.JoinRoom.getId(), resObj, user);
		}
	}

}
