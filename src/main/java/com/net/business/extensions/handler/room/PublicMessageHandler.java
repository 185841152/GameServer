package com.net.business.extensions.handler.room;

import com.net.business.extensions.core.BaseClientRequestHandler;
import com.net.server.controllers.SystemRequest;
import com.net.server.data.IGameObject;
import com.net.server.entities.User;

import java.util.Iterator;
import java.util.List;

public class PublicMessageHandler extends BaseClientRequestHandler {

	@Override
	public void handleClientRequest(User user, IGameObject params) throws Exception {
		try {
			List<User> users= (List<User>) user.getLastJoinedRoom().getUserList();
			Iterator<User> userIterator=users.iterator();
			while(userIterator.hasNext()){
				User u=userIterator.next();
				if (!u.isConnected()){
					userIterator.remove();
				}
			}
			sendResponse(SystemRequest.PublicMessage.getId(), params, users);
		} catch (Exception e) {
			
		}
		
	}

}
