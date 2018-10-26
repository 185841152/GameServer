package com.net.business.extensions.handler.room;

import com.net.business.basedata.BaseDataManager;
import com.net.business.entity.AppUser;
import com.net.business.entity.Chapter;
import com.net.business.entity.GameData;
import com.net.business.entity.UserChapter;
import com.net.business.extensions.core.BaseClientRequestHandler;
import com.net.business.extensions.handler.Constants;
import com.net.business.extensions.handler.GameRunner;
import com.net.server.controllers.SystemRequest;
import com.net.server.data.GameObject;
import com.net.server.data.IGameObject;
import com.net.server.entities.Room;
import com.net.server.entities.User;
import com.net.server.exceptions.ErrorCode;
import com.net.server.exceptions.ErrorData;
import com.net.server.exceptions.GameException;
import io.netty.util.concurrent.ScheduledFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StartGameHandler extends BaseClientRequestHandler {
    private Logger logger = LoggerFactory.getLogger(StartGameHandler.class);

    @Override
    public void handleClientRequest(User user, IGameObject params) throws Exception {
        IGameObject result = GameObject.newInstance();
        try {
            Room room = user.getLastJoinedRoom();
            if (room != null) {
                int status = (int) room.getProperty("status");
                if(status != Constants.RoomStatus.Wait.getStatus()){
                    ErrorData errData = new ErrorData(ErrorCode.ROOM_NOT_EXITS);
                    GameException err = new GameException("不能重复开始游戏", errData);
                    throw err;
                }
                AppUser appuser = (AppUser) user.getProperty("userInfo");
                UserChapter userChapter = appuser.getUserChapter();

                int currentChapter = 1;
                if(userChapter != null){
                    currentChapter = userChapter.getChapter();
                }else{
                    UserChapter startChapter = new UserChapter();
                    startChapter.setComplate(false);
                    userChapter = startChapter;
                }
                if (!userChapter.isComplate()) {
                    currentChapter = userChapter.getChapter() - 1;
                }
                if (userChapter != null)
                    room.setProperty("chapter", currentChapter);
                Chapter chapter = BaseDataManager.getInstance().getChapter(currentChapter);
                result.putInt("total", chapter.getTotal());

                GameRunner gameRunner = new GameRunner(room, getParentExtension());
                result.putInt("hp", chapter.getHp());
                ScheduledFuture<?> scheduledFuture = getParentExtension().addTask(room.getId(), gameRunner, 0, 1);
                room.setProperty("schedule", scheduledFuture);
                GameData gameData = new GameData(currentChapter,chapter.getTotal(),chapter.getHp());//创建房间信息
                room.setProperty("data",gameData);
                result.putInt("currentChapter", currentChapter);
                room.setProperty("runner",gameRunner);
                sendResponse(SystemRequest.StartGame.getId(), result, room.getUserList());
                room.setProperty("status", Constants.RoomStatus.InGame.getStatus());
            }
        } catch (Exception e) {
            this.logger.error("创建房间出错:{},{}", user.getId(), e);
            result.putShort("ec", Short.valueOf("500"));
            sendResponse(SystemRequest.StartGame.getId(), result, user);
        }

    }

}
