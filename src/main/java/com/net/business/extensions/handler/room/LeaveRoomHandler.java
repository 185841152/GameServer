package com.net.business.extensions.handler.room;

import com.net.business.extensions.core.BaseClientRequestHandler;
import com.net.server.GameServer;
import com.net.server.controllers.SystemRequest;
import com.net.server.data.GameObject;
import com.net.server.data.IGameObject;
import com.net.server.entities.Room;
import com.net.server.entities.User;
import io.netty.util.concurrent.ScheduledFuture;

import java.util.List;

public class LeaveRoomHandler extends BaseClientRequestHandler {


    @Override
    public void handleClientRequest(User user, IGameObject params) throws Exception {
        Room room = user.getLastJoinedRoom();
        if (room != null) {
            if (room.getOwner() == user) {
                //销毁房间所有信息
                GameServer.getInstance().getAPIManager().getSFSApi().removeRoom(room);
                List<User> users = room.getUserList();
                for (User u : users) {
                    room.removeUser(u);
                }
                //停止计时器操作
                ScheduledFuture<?> scheduledFuture = (ScheduledFuture<?>) room.getProperty("schedule");
                if (scheduledFuture != null) {
                    scheduledFuture.cancel(true);
                }
            } else {
                List<User> users = user.getLastJoinedRoom().getUserList();
                boolean isConnected = true;
                //是否所有人不在线
                for (User u : users) {
                    if (u.isConnected()) {
                        isConnected = false;
                        break;
                    }
                }
                //如果所有人离线
                if (isConnected) {
                    for (User u : users) {
                        room.removeUser(u);
                    }
                    //停止计时器操作
                    ScheduledFuture<?> scheduledFuture = (ScheduledFuture<?>) room.getProperty("schedule");
                    if (scheduledFuture != null) {
                        scheduledFuture.cancel(true);
                    }
                    //销毁房间所有信息
                    GameServer.getInstance().getAPIManager().getSFSApi().removeRoom(room);
                } else {
                    GameServer.getInstance().getAPIManager().getSFSApi().leaveRoom(user,room);
                    sendResponse(SystemRequest.LeaveRoom.getId(), GameObject.newInstance(), user);
                }
            }
        }
    }
}
