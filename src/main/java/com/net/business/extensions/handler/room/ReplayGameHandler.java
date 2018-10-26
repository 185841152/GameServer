package com.net.business.extensions.handler.room;

import com.net.business.entity.GameData;
import com.net.business.extensions.core.BaseClientRequestHandler;
import com.net.business.extensions.handler.GameRunner;
import com.net.server.controllers.SystemRequest;
import com.net.server.data.GameObject;
import com.net.server.data.IGameObject;
import com.net.server.entities.Room;
import com.net.server.entities.User;

public class ReplayGameHandler extends BaseClientRequestHandler {


    @Override
    public void handleClientRequest(User user, IGameObject params) throws Exception {
        Room room = user.getLastJoinedRoom();
        //判断是否是房主
        if(room.getOwner() == user){
            GameRunner gameRunner = (GameRunner) room.getProperty("runner");
            GameData data = (GameData) room.getProperty("data");
            data.init();
            gameRunner.initGame(0,0);
            IGameObject result = GameObject.newInstance();
            result.putInt("currentChapter", data.getCurrentChapter());
            result.putInt("total", data.getTotal());
            result.putInt("hp", data.getAllHp());
            sendResponse(SystemRequest.StartGame.getId(), result, room.getUserList());
        }
    }
}
