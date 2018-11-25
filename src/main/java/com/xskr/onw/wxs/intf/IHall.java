package com.xskr.onw.wxs.intf;

import com.xskr.onw.wxs.core.Room;
import com.xskr.onw.wxs.core.WxUser;

public interface IHall {

    int create();

    void join(WxUser wxUser, int roomID);

    void leave(WxUser wxUser, int roomID);

    Room get(int id);

}
