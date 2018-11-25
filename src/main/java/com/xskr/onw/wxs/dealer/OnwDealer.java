package com.xskr.onw.wxs.dealer;

import com.xskr.onw.wxs.core.WxUser;
import com.xskr.onw.wxs.rx.RxOnwRoom;

public abstract class OnwDealer {

    protected RxOnwRoom room;

    public OnwDealer(RxOnwRoom room){
        this.room = room;
    }

    public abstract void join(WxUser user);
    public abstract void leave(WxUser user);
    public abstract void pickDesktopCard(WxUser user, int id);
    public abstract void pickReady(WxUser user);
    public abstract void pickRoleCard(WxUser user, int id);
    public abstract void pickSeat(WxUser user, int id);
    public abstract void initialize();
}
