package com.net.business.entity;

import java.util.List;

public class Chapter {
    public static final String ENTITY_NAME = "MiniGameChapter";

    private String name;
    private int hp;
    private String description;
    private List<Monster> spawns;
    private int total;
    private List<Items> items;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Monster> getSpawns() {
        return spawns;
    }

    public void setSpawns(List<Monster> spawns) {
        this.spawns = spawns;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<Items> getItems() {
        return items;
    }

    public void setItems(List<Items> items) {
        this.items = items;
    }

    public int whenTimeItem(int time){
        if(items == null){
            return -1;
        }
        for(int i=0;i<this.items.size();i++){
            if(items.get(i).getOutTime() == time){
                return items.get(i).getIndex();
            }
        }
        return -1;
    }
}
