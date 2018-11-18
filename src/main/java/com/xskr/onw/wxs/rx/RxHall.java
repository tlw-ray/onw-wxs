package com.xskr.onw.wxs.rx;

import com.xskr.onw.wxs.core.Room;
import com.xskr.onw.wxs.core.WxUser;
import io.reactivex.Observable;
import io.reactivex.Observer;

import java.util.HashMap;
import java.util.Map;

public class RxHall{

    private int ROOM_ID_GENERATOR = 0;
    private Map<Integer, Room> idRoomMap = new HashMap();

    public int create(){

        return 0;
    }

    public void join(WxUser wxUser, int roomID){

    }

    public void leave(WxUser wxUser, int roomID){

    }

    public Room get(int id){

        return null;
    }
}
