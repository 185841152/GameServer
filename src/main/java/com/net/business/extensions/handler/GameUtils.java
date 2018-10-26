package com.net.business.extensions.handler;

import com.net.business.basedata.BaseDataManager;
import com.net.business.entity.Chapter;
import com.net.business.entity.GameData;
import com.net.business.extensions.core.GameExtension;
import com.net.server.controllers.SystemRequest;
import com.net.server.data.GameObject;
import com.net.server.data.IGameObject;
import com.net.server.entities.Room;
import com.net.server.entities.User;

import java.util.Iterator;
import java.util.List;

public class GameUtils {

    /**
     * @param gameData 房间信息
     * @param id      小怪的id
     * @return 是否成功杀死
     */
    public static boolean killMonster(GameData gameData,int id){
        //移除小怪的id
        boolean isSuccess = gameData.killMonster(id);
        if(isSuccess){
            //添加小怪死亡数量
            gameData.addKilled();
            return true;
        }
        return false;
    }

    /**
     * @param gameData 房间信息
     * @param id 小怪的id
     * @return   是否游戏结束
     */
    public synchronized static boolean attackTower(GameData gameData,int id){
        //移除小怪的id
        boolean isSuccess = gameData.killMonster(id);
        if(isSuccess){
            //塔失去血量
            gameData.lostHP(10);
            //添加小怪死亡数量
            gameData.addKilled();
            gameData.addTowerKilled();
            if(gameData.getHp() <= 0){
                //游戏结束
                return true;
            }
        }
        return false;
    }

    /**
     * 发送游戏结束通知
     * @param user
     * @param extension
     */
    public static void sendGameFinish(User user, GameExtension extension){
        List<User> users= (List<User>) user.getLastJoinedRoom().getUserList();
        Iterator<User> userIterator=users.iterator();
        while(userIterator.hasNext()){
            User u=userIterator.next();
            if (!u.isConnected()){
                userIterator.remove();
            }
        }
        extension.send(SystemRequest.GameFinish.getId(), GameObject.newInstance(),users);
    }

    /**
     * 发送游戏失败通知
     * @param user
     * @param extension
     */
    public static void sendGameOver(User user, GameExtension extension){
        List<User> users= (List<User>) user.getLastJoinedRoom().getUserList();
        Iterator<User> userIterator=users.iterator();
        while(userIterator.hasNext()){
            User u=userIterator.next();
            if (!u.isConnected()){
                userIterator.remove();
            }
        }
        extension.send(SystemRequest.GameOver.getId(), GameObject.newInstance(),users);
    }

   public static void nextChapterHandler(User user,GameExtension extension){
       Room room = user.getLastJoinedRoom();
       GameData data = (GameData) room.getProperty("data");
       if(data != null){
           IGameObject result = GameObject.newInstance();
           data.setStop(false);
           int currentChapter = data.getCurrentChapter()+1;
           room.setProperty("chapter", currentChapter);
           Chapter chapter = BaseDataManager.getInstance().getChapter(currentChapter);
           result.putInt("total", chapter.getTotal());

           result.putInt("hp", chapter.getHp());
           GameRunner runner = (GameRunner) room.getProperty("runner");
           if(runner != null){
               runner.initGame(0,0);
           }
           GameData gameData = new GameData(currentChapter,chapter.getTotal(),chapter.getHp());//创建房间信息
           room.setProperty("data",gameData);
           result.putInt("currentChapter", currentChapter);
           extension.send(SystemRequest.StartGame.getId(), result, room.getUserList());
           room.setProperty("status", Constants.RoomStatus.InGame.getStatus());
       }
   }

}
