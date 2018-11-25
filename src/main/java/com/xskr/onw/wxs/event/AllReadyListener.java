package com.xskr.onw.wxs.event;

import com.xskr.onw.wxs.rx.RxRoom;

import java.util.EventListener;

public interface AllReadyListener extends EventListener {
    void afterAllReady(RxRoom room);
}
