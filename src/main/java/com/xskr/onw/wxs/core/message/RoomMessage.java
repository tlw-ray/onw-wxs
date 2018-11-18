package com.xskr.onw.wxs.core.message;

import com.xskr.onw.wxs.core.Room;

public class RoomMessage extends AbsMessage {

    protected Room room;

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }
}
