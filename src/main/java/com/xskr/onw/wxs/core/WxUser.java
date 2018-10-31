package com.xskr.onw.wxs.core;

import java.util.Objects;

public class WxUser {

    private String openid;
    private String nickName;
    private String avatarUrl;

    public WxUser(String openid, String nickName, String avatarUrl) {
        this.openid = openid;
        this.nickName = nickName;
        this.avatarUrl = avatarUrl;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WxUser wxUser = (WxUser) o;
        return Objects.equals(openid, wxUser.openid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(openid);
    }

    @Override
    public String toString() {
        return "WxUser{" +
                "openid='" + openid + '\'' +
                ", nickName='" + nickName + '\'' +
                ", avatarUrl='" + avatarUrl + '\'' +
                '}';
    }
}
