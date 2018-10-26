package com.net.business.entity;

import com.net.server.data.SerializableGameType;

public class UserChapter implements SerializableGameType {
    private int chapter=1;
    private int kill=0;
    private boolean complate;

    public int getChapter() {
        return chapter;
    }

    public void setChapter(int chapter) {
        this.chapter = chapter;
    }

    public int getKill() {
        return kill;
    }

    public void setKill(int kill) {
        this.kill = kill;
    }

    public boolean isComplate() {
        return complate;
    }

    public void setComplate(boolean complate) {
        this.complate = complate;
    }
}
