package com.net.business.entity;

import com.net.server.data.SerializableGameType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 方法描述:小程序用户信息
 * <p>
 * author 小刘
 * version v1.0
 * date 2017/8/10 14:32
 */
public class AppUser implements SerializableGameType{
    public static final String ENTITY_NAME = "MiniGameUser";
    private long id;
    private String appId;
    private String openId;
    private String unionId;
    private String nickName;
    private String avatarUrl;
    private int gender;
    private String province;
    private String city;
    private String country;
    private Date updateTime;
    private Date createTime;
    private int gold;
    private int loginDay = 1;
    private int equip;//当前解锁的装备
    private boolean reward;//是否领奖
    private boolean share;//是否分享
    private int rewardGold;//奖励金币
    private int rewardItem;//奖励道具

    private UserChapter userChapter;
    private List<Integer> items=new ArrayList<>();

    public boolean isShare() {
        return share;
    }

    public void setShare(boolean share) {
        this.share = share;
    }

    public List<Integer> getItems() {
        return items;
    }

    public void setItems(List<Integer> items) {
        this.items = items;
    }

    public UserChapter getUserChapter() {
        return userChapter;
    }

    public void setUserChapter(UserChapter userChapter) {
        this.userChapter = userChapter;
    }

    public int getEquip() {
        return equip;
    }

    public void setEquip(int equip) {
        this.equip = equip;
    }

    public boolean isReward() {
        return reward;
    }

    public void setReward(boolean reward) {
        this.reward = reward;
    }

    public int getRewardGold() {
        return rewardGold;
    }

    public void setRewardGold(int rewardGold) {
        this.rewardGold = rewardGold;
    }

    public int getRewardItem() {
        return rewardItem;
    }

    public void setRewardItem(int rewardItem) {
        this.rewardItem = rewardItem;
    }

    public int getLoginDay() {
        return loginDay;
    }

    public void setLoginDay(int loginDay) {
        this.loginDay = loginDay;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getUnionId() {
        return unionId;
    }

    public void setUnionId(String unionId) {
        this.unionId = unionId;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public int getGold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }
}
