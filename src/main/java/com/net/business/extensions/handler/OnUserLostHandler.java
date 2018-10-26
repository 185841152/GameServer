package com.net.business.extensions.handler;

import com.net.business.extensions.core.BaseClientRequestHandler;
import com.net.server.GameServer;
import com.net.server.controllers.SystemRequest;
import com.net.server.data.GameObject;
import com.net.server.data.IGameObject;
import com.net.server.entities.Room;
import com.net.server.entities.User;
import io.netty.util.concurrent.ScheduledFuture;

import java.util.List;

public class OnUserLostHandler extends BaseClientRequestHandler {

    @Override
    public void handleClientRequest(User user, IGameObject params) throws Exception {
        user.setConnected(false);
        if (user.getLastJoinedRoom() != null) {
            List<User> users = user.getLastJoinedRoom().getUserList();
            Room room = user.getLastJoinedRoom();
            boolean isConnected = true;
            //是否所有人不在线
            for (User u : users) {
                if (u.isConnected()) {
                    isConnected = false;
                    break;
                }
            }
            if (isConnected) {
                //销毁房间所有信息
                GameServer.getInstance().getAPIManager().getSFSApi().removeRoom(room);
                for (User u : users) {
                    room.removeUser(u);
                }
                //停止计时器操作
                ScheduledFuture<?> scheduledFuture = (ScheduledFuture<?>) room.getProperty("schedule");
                if (scheduledFuture != null) {
                    scheduledFuture.cancel(true);
                }
            } else {
                users.remove(user);
                //发送用户离线通知
                IGameObject userOffline = GameObject.newInstance();
                userOffline.putInt("idx", (int) user.getProperty("idx"));
                sendResponse(SystemRequest.OnUserLost.getId(), userOffline, users);
            }
        }
    }

}
