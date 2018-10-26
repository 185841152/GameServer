package com.net.business.extensions.handler.room;

import com.net.business.entity.GameData;
import com.net.business.extensions.core.BaseClientRequestHandler;
import com.net.server.controllers.SystemRequest;
import com.net.server.data.IGameObject;
import com.net.server.entities.Room;
import com.net.server.entities.User;

import java.util.Iterator;
import java.util.List;

public class UseToolHandler extends BaseClientRequestHandler{

    @Override
    public void handleClientRequest(User user, IGameObject params) throws Exception {
        int item = params.getInt("item");
        try {
            Room room = user.getLastJoinedRoom();
            if(item == 0){
                if(room != null){
                    GameData data = (GameData) room.getProperty("data");
                    data.addHp(80);
                }
            }
            List<User> users= (List<User>) room.getUserList();
            Iterator<User> userIterator=users.iterator();
            while(userIterator.hasNext()){
                User u=userIterator.next();
                if (!u.isConnected()){
                    userIterator.remove();
                }
            }
            sendResponse(SystemRequest.UseTool.getId(), params, users);
        } catch (Exception e) {

        }

    }


}
