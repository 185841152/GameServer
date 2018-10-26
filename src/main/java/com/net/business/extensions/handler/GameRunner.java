package com.net.business.extensions.handler;

import com.net.business.basedata.BaseDataManager;
import com.net.business.entity.Chapter;
import com.net.business.entity.GameData;
import com.net.business.entity.Monster;
import com.net.business.extensions.core.GameExtension;
import com.net.server.GameServer;
import com.net.server.controllers.SystemRequest;
import com.net.server.data.GameArray;
import com.net.server.data.GameObject;
import com.net.server.data.IGameArray;
import com.net.server.entities.Room;
import com.net.server.entities.User;
import io.netty.util.concurrent.ScheduledFuture;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class GameRunner implements Runnable{

    private Room room;

    private GameExtension extension;

    private int gameTime = 0;

    private AtomicInteger currentMonsterId ;

    public GameRunner(Room room,GameExtension extension) {
        this.room=room;
        this.extension=extension;
        this.currentMonsterId = new AtomicInteger();
    }
    @Override
    public void run() {
        GameData data = (GameData) room.getProperty("data");
        //验证是否暂停游戏
        if(data.isStop()){
            return ;
        }
        gameTime++;
        System.out.println("时间线------》》》》》"+gameTime);
        int currentChapter = (int) room.getProperty("chapter");
        Chapter chapter= BaseDataManager.getInstance().getChapter(currentChapter);
        int itemIndex = chapter.whenTimeItem(gameTime);
        //所有的小怪的id
        List<Integer> allIds = new ArrayList<>();
        if(chapter!=null){
            Random random=new Random();
            IGameArray monsters = GameArray.newInstance();
            for (int i=0;i<chapter.getSpawns().size();i++){
                Monster monster=chapter.getSpawns().get(i);
                if (monster.getOutTime()==gameTime){
                    for (int j=0;j<monster.getCount();j++){
                        GameObject mst=GameObject.newInstance();
                        int x=1334+random.nextInt(200);
                        int y=random.nextInt(200)+30;
                        if(!isGround(monster.getIdx())){
                            boolean top=random.nextBoolean();
                            if(top){//从屏幕上方飞出
                                x=667+random.nextInt(667);
                                y=750;
                            }else{
                                x=1334+random.nextInt(200);
                                y=375+random.nextInt(375);
                            }
                        }
                        //小怪的id
                        this.currentMonsterId.incrementAndGet();
                        allIds.add(currentMonsterId.get());
                        mst.putInt("id",currentMonsterId.get());
                        mst.putInt("idx",monster.getIdx());
                        mst.putInt("x",x);
                        mst.putInt("y",y);
                        mst.putInt("direction",monster.getDirection());
                        mst.putInt("level",monster.getLevel());
                        mst.putInt("speed",monster.getSpeed());
                        mst.putInt("item",monster.getItem());
                        mst.putDouble("size",monster.getSize());
                        mst.putUtfString("color",monster.getColor());
                        monsters.addGameObject(mst);
                    }
                }
            }
            if (monsters.size()>0 || itemIndex>=0){
                //发送刷怪消息
                GameObject msg=GameObject.newInstance();
                if(monsters.size()>0){
                    data.addIds(allIds);
                    msg.putGameArray("ms",monsters);
                }
                if(itemIndex>=0){
                    msg.putInt("item",itemIndex);
                }
                extension.send(SystemRequest.OutMonster.getId(),msg,room.getUserList());
            }
            if(gameTime >= 100){
                List<User> users = room.getUserList();
                //销毁房间所有信息
                GameServer.getInstance().getAPIManager().getSFSApi().removeRoom(room);
                for (User u : users) {
                    room.removeUser(u);
                }
                ScheduledFuture<?> scheduledFuture = (ScheduledFuture<?>) room.getProperty("schedule");
                scheduledFuture.cancel(true);
            }
        }
    }

    private boolean isGround(int idx){
        if(idx==7 ||idx==8 ||idx==9 ||idx==10 ||idx==11 ||idx==14 ){
            return false;
        }
        return true;
    }

    public void setGameTime(int time){
        this.gameTime = time;
    }


    public void initGame(int gameTime,int currentMonsterId){
        this.gameTime = gameTime;
        this.currentMonsterId.set(currentMonsterId);
    }
}
