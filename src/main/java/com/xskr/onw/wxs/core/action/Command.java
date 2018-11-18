package com.xskr.onw.wxs.core.action;

public enum Command {
    //user room
    CREATE_ROOM, JOIN_ROOM, LEAVE_ROOM,
    //user to seat
    SIT_DOWN_SEAT, STAND_UP_SEAT, READY_SEAT, PICK_SEAT, VOTE_SEAT,
    //user to desktop
    PICK_DESKTOP_CARD
}
