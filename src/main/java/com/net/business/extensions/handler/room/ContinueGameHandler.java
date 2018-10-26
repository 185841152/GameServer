package com.net.business.extensions.handler.room;

import com.net.business.entity.GameData;
import com.net.business.extensions.core.BaseClientRequestHandler;
import com.net.server.controllers.SystemRequest;
import com.net.server.data.GameObject;
import com.net.server.data.IGameObject;
import com.net.server.entities.Room;
import com.net.server.entities.User;
import com.net.server.exceptions.ErrorCode;
import com.net.server.exceptions.ErrorData;
import com.net.server.exceptions.GameException;

import java.util.Iterator;
import java.util.List;

public class ContinueGameHandler extends BaseClientRequestHandler{

    @Override
    public void handleClientRequest(User user, IGameObject params) throws Exception {
        IGameObject result = GameObject.newInstance();
        try{
            Room room = user.getLastJoinedRoom();
            if(room != null){
                if(room.getOwner() != user){
                    ErrorData errData = new ErrorData(ErrorCode.ROOM_NOT_EXITS);
                    GameException err = new GameException("房主才能开始游戏", errData);
                    throw err;
                }
                int status = (int) room.getProperty("status");
                GameData data = (GameData) room.getProperty("data");
                if(data != null){
                    data.setStop(false);
                    data.setHp(data.getAllHp());
                    List<User> users= (List<User>) user.getZone().getUserList();
                    Iterator<User> userIterator = users.iterator();
                    while(userIterator.hasNext()){
                        User u=userIterator.next();
                        if (!u.isConnected()){
                            userIterator.remove();
                        }
                    }
                    result.putInt("hp",data.getAllHp());
                    sendResponse(SystemRequest.ContinueGame.getId(),result, users);
                }
            }
        }catch (GameException e){
            result.putShort("ec", Short.valueOf("500"));
            sendResponse(SystemRequest.ContinueGame.getId(), result, user);
        }
    }
}
