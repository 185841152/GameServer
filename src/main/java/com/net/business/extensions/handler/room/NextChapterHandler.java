package com.net.business.extensions.handler.room;


import com.net.business.extensions.core.BaseClientRequestHandler;
import com.net.business.extensions.handler.GameUtils;
import com.net.server.data.IGameObject;
import com.net.server.entities.Room;
import com.net.server.entities.User;
import com.net.server.exceptions.ErrorCode;
import com.net.server.exceptions.ErrorData;
import com.net.server.exceptions.GameException;

/**
 * 开始下一关的调用
 */
public class NextChapterHandler extends BaseClientRequestHandler {


    @Override
    public void handleClientRequest(User user, IGameObject params) throws Exception {
        Room room = user.getLastJoinedRoom();
        //重新设置玩家的关卡的层数  更新分数
        if(room != null){
            if(room.getOwner() != user){
                ErrorData errData = new ErrorData(ErrorCode.ROOM_NOT_EXITS);
				GameException err = new GameException("房主才能开启下一关", errData);
				throw err;
            }
            GameUtils.nextChapterHandler(user,getParentExtension());
        }
    }




}
