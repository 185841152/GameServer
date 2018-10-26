package com.net.business.extensions.handler.room;

import com.net.business.entity.GameData;
import com.net.business.extensions.core.BaseClientRequestHandler;
import com.net.business.extensions.handler.Constants;
import com.net.business.extensions.handler.GameUtils;
import com.net.server.data.IGameObject;
import com.net.server.entities.Room;
import com.net.server.entities.User;

public class KillMonsterHandler extends BaseClientRequestHandler{

    @Override
    public void handleClientRequest(User user, IGameObject params) throws Exception {
        Room room = user.getLastJoinedRoom();
        int monsterId = params.getInt("id");
        boolean b = params.getBool("b");
        if(room != null){
            //获取游戏信息
            GameData gamedata = (GameData) room.getProperty("data");
            //是否是撞塔
            if(b){
                boolean isOver = GameUtils.attackTower(gamedata,monsterId);
                if(isOver){
                    gamedata.setStop(true);
                    GameUtils.sendGameOver(user,getParentExtension());
                    room.setProperty("status", Constants.RoomStatus.WaitReplay.getStatus());
                    return ;
                }
                //判断是否游戏完成
                boolean finishGame = gamedata.isFinishGame();
                if(finishGame){
                    gamedata.setStop(true);
                    //发送游戏结束通知
                    GameUtils.sendGameFinish(user,getParentExtension());
                    //更改房间状态
                    room.setProperty("status", Constants.RoomStatus.WaitNext.getStatus());
                }
            }else{
                //是否成功击杀
                boolean isSuccess = GameUtils.killMonster(gamedata, monsterId);
                if(isSuccess){
                    //判断是否游戏完成
                    boolean finishGame = gamedata.isFinishGame();
                    if(finishGame){
                        gamedata.setStop(true);
                        //发送游戏结束通知
                        GameUtils.sendGameFinish(user,getParentExtension());
                        //更改房间状态
                        room.setProperty("status", Constants.RoomStatus.WaitNext.getStatus());
                    }
                }
            }
        }
    }

}
