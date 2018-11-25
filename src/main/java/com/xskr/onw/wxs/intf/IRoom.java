package com.xskr.onw.wxs.intf;

import com.xskr.onw.wxs.core.WxUser;
import com.xskr.onw.wxs.core.message.OnwMessage;

public interface IRoom {

    void onAllReady();

    void onAllVoted();

    void sendMessage(WxUser wxUser, OnwMessage message);

    void sendMessage(OnwMessage message);

}
