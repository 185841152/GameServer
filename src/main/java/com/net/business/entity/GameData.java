package com.net.business.entity;

import com.net.server.data.GameObject;
import com.net.server.data.IGameObject;

import java.util.ArrayList;
import java.util.List;

public class GameData {

    private int currentChapter;//当前关卡
    private int total;//小怪的总数
    private int countKilled;//小怪死亡的总数量  包括撞击建筑物
    private int countBirth;//已经产生的小怪数量
    private int allHp;//防御塔总血量
    private int hp;//当前防御塔的血量
    private int towerKilled;//防御塔杀死小怪的数量
    private List<Integer> list;//当前存活小怪的所有id
    private boolean isStop;//是否暂停游戏


    public GameData(int currentChapter, int total, int allHp) {
        this.currentChapter = currentChapter;
        this.countKilled = 0;
        this.countBirth = 0;
        this.towerKilled = 0;
        this.total = total;
        this.hp = allHp;
        this.allHp = allHp;
        this.isStop = false;
        list = new ArrayList<>();
    }

    public void init(){
        this.hp = this.allHp;
        this.countKilled = 0;
        this.towerKilled = 0;
        this.countBirth = 0;
        this.isStop = false;
        this.list.clear();
    }

    public int getCurrentChapter() {
        return currentChapter;
    }

    public void setCurrentChapter(int currentChapter) {
        this.currentChapter = currentChapter;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getCountKilled() {
        return countKilled;
    }

    public void setCountKilled(int countKilled) {
        this.countKilled = countKilled;
    }

    public int getCountBirth() {
        return countBirth;
    }

    public void setCountBirth(int countBirth) {
        this.countBirth = countBirth;
    }

    public int getAllHp() {
        return allHp;
    }

    public void setAllHp(int allHp) {
        this.allHp = allHp;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public void addHp(int hp){
        this.hp+=hp;
        System.out.println("当前防御塔血量---->>>>>>"+this.hp);
    }

    public int getTowerKilled() {
        return towerKilled;
    }

    public void setTowerKilled(int towerKilled) {
        this.towerKilled = towerKilled;
    }

    public List<Integer> getList() {
        return list;
    }

    public void setList(List<Integer> list) {
        this.list = list;
    }

    public boolean killMonster(Integer id) {
        if(list.contains(id)){
            list.remove(id);
            System.out.println("小怪总数:"+this.total);
            System.out.println("小怪被击杀数:"+this.countKilled);
            return true;
        }
        return false;
    }

    public void addKilled(){
        countKilled++;
    }

    public void addTowerKilled(){
        towerKilled++;
    }


    /*是否完成游戏*/
    public boolean isFinishGame(){
        if(countKilled >= total){
            return true;
        }
        return false;
    }


    public void lostHP(int hp){
        this.hp-=hp;
        System.out.println("当前防御塔血量------》》》》"+this.hp);
    }

    public void addIds(List<Integer> ids){
        list.addAll(ids);
    }

    public void setStop(boolean stop) {
        isStop = stop;
    }

    public boolean isStop() {
        return isStop;
    }

    public IGameObject toGameObject(){
        IGameObject gameObject = GameObject.newInstance();
        //是否暂停
        gameObject.putBool("isStop",isStop);
        //怪物总数
        gameObject.putInt("total",total);
        //被杀死的总数
        gameObject.putInt("killed",countKilled);
        //塔杀死的总数
        gameObject.putInt("towerKilled",towerKilled);
        //关卡
        gameObject.putInt("currentChapter",currentChapter);
        //防御塔血量
        gameObject.putInt("hp",hp);
        return gameObject;
    }


}
