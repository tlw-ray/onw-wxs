package com.xskr.onw.wxs.rx;

import com.xskr.onw.wxs.core.WxUser;
import com.xskr.onw.wxs.core.message.OnwMessage;

public interface IRoom {

    void join(WxUser wxUser);

    void leave(String openid);

    void switchReady(String openid);

    void pickRoleCard(String openid, int location);

    void pickDesktopCard(String openid, int location);

    void pickSeat(String openid, int location);

    void sendMessage(String openid, OnwMessage message);

    void sendMessage(OnwMessage message);

}
