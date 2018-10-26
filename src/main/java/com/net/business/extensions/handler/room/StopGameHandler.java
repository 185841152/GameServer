package com.net.business.extensions.handler.room;

import com.net.business.entity.GameData;
import com.net.business.extensions.core.BaseClientRequestHandler;
import com.net.business.extensions.handler.Constants;
import com.net.server.controllers.SystemRequest;
import com.net.server.data.IGameObject;
import com.net.server.entities.Room;
import com.net.server.entities.User;

/**
 * 暂停游戏或者暂停游戏
 */
public class StopGameHandler extends BaseClientRequestHandler{

    @Override
    public void handleClientRequest(User user, IGameObject params) throws Exception {
        Room room = user.getLastJoinedRoom();
        if(user != room.getOwner()){
            //不是房主 无法继续游戏或者暂停游戏
            return ;
        }
        Boolean isStop = params.getBool("isStop");
        //暂停出怪
        if(room != null){
            int status = (int) room.getProperty("status");
            if(status != Constants.RoomStatus.InGame.getStatus()){
                return ;
            }
            GameData data = (GameData) room.getProperty("data");
            if(data != null){
                if(data.isStop() != isStop){
                    data.setStop(isStop);
                    sendResponse(SystemRequest.StopGame.getId(), params,room.getUserList());
                    if(isStop = true){
                        room.setProperty("status", Constants.RoomStatus.Stop.getStatus());
                    }
                }
            }
        }
    }
}
