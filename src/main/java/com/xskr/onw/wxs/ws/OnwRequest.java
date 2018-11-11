//package com.xskr.onw.wxs.ws;
//
///**
// * 所有可能请求的交集
// * 用路径可能是更好的表述方式, 但webSocket没有路径映射
// */
//public class OnwRequest {
//
//    public static final String ENTITY_HALL = "hall";
//    public static final String ENTITY_ROOM = "room";
//
//    //Hall command
//    public static final String COMMAND_CREATE = "create";
//    public static final String COMMAND_JOIN = "join";
//    public static final String COMMAND_LEAVE = "leave";
//
//    //Room command
//    public static final String COMMAND_INFO = "info";
//    public static final String COMMAND_DESKTOP_CARD = "desktopCard";
//    public static final String COMMAND_READY = "ready";
//    public static final String COMMAND_SEAT = "seat";
//    public static final String COMMAND_
//
//    //实体路径例如/onw/hall或者/onw/room
//    protected String entity;
//    protected String command;
//
//    //通用参数
//    protected String openid;
//    protected int roomID;
//
//    //Hall参数
//    protected String nickName;
//    protected String avatarURL;
//
//    //Room参数
//    protected int[] ids;
//    protected boolean ready;
//
//    public String getEntity() {
//        return entity;
//    }
//
//    public void setEntity(String entity) {
//        this.entity = entity;
//    }
//
//    public String getCommand() {
//        return command;
//    }
//
//    public void setCommand(String command) {
//        this.command = command;
//    }
//
//    public String getOpenid() {
//        return openid;
//    }
//
//    public void setOpenid(String openid) {
//        this.openid = openid;
//    }
//
//    public int getRoomID() {
//        return roomID;
//    }
//
//    public void setRoomID(int roomID) {
//        this.roomID = roomID;
//    }
//
//    public String getNickName() {
//        return nickName;
//    }
//
//    public void setNickName(String nickName) {
//        this.nickName = nickName;
//    }
//
//    public String getAvatarURL() {
//        return avatarURL;
//    }
//
//    public void setAvatarURL(String avatarURL) {
//        this.avatarURL = avatarURL;
//    }
//
//    public String getRoleCardID() {
//        return roleCardID;
//    }
//
//    public void setRoleCardID(String roleCardID) {
//        this.roleCardID = roleCardID;
//    }
//
//    public String getDesktopCardID() {
//        return desktopCardID;
//    }
//
//    public void setDesktopCardID(String desktopCardID) {
//        this.desktopCardID = desktopCardID;
//    }
//
//    public String getSeatID() {
//        return seatID;
//    }
//
//    public void setSeatID(String seatID) {
//        this.seatID = seatID;
//    }
//
//    public boolean isReady() {
//        return ready;
//    }
//
//    public void setReady(boolean ready) {
//        this.ready = ready;
//    }
//}
