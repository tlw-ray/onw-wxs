package com.xskr.onw.wxs.event;

import com.xskr.onw.wxs.rx.RxOnwRoom;

import java.util.EventListener;

public interface AllVoteListener extends EventListener {
    void afterAllVoted(RxOnwRoom room);
}
