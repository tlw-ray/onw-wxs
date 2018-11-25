package com.xskr.onw.wxs.event;

import com.xskr.onw.wxs.rx.RxOnwRoom;

import java.util.EventListener;

public interface AvatarListener extends EventListener {
    void afterAvatar(RxOnwRoom room);
}
