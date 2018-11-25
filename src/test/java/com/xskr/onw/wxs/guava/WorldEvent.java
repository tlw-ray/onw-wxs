package com.xskr.onw.wxs.guava;

import lombok.Data;

@Data
public class WorldEvent extends HelloEvent{
    private int eventNo;

    public WorldEvent(String name, int eventNo){
        super(name);
        this.eventNo = eventNo;
    }
}
