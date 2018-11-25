package com.xskr.onw.wxs.rx;

import com.xskr.onw.wxs.core.WxUser;

import java.util.HashMap;
import java.util.Map;

public class RxHall{

    private int ROOM_ID_GENERATOR = 0;
    private Map<Integer, RxOnwRoom> idRoomMap = new HashMap();

    public synchronized int create(){
        RxOnwRoom rxOnwDealer = new RxOnwRoom(ROOM_ID_GENERATOR);
        idRoomMap.put(ROOM_ID_GENERATOR, rxOnwDealer);
        int result = ROOM_ID_GENERATOR;
        ROOM_ID_GENERATOR++;
        return result;
    }

    public synchronized void join(WxUser wxUser, int roomID){
        RxOnwRoom rxOnwDealer = get(roomID);
        rxOnwDealer.join(wxUser);
    }

    public synchronized void leave(WxUser wxUser, int roomID){
        RxOnwRoom rxOnwDealer = get(roomID);
        rxOnwDealer.leave(wxUser);
    }

    public synchronized RxOnwRoom get(int id){
        return idRoomMap.get(id);
    }
}
