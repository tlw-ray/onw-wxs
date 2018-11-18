package com.xskr.onw.wxs.core.message;

import com.xskr.onw.wxs.core.ClientAction;

import java.util.Date;

public class OnwMessage {

    //要传递的消息
    private String message;
    //要执行的指令
    private ClientAction action;
    //要传递的数据
    private Object data;
    //时间戳
    private Date date = new Date();
//    //是否是游戏信息，消息可分为提示信息、控制信息、游戏信息，
//    //提示信息告知无关紧要的信息，例如有人进入退出房间了
//    //控制信息告知客户端接下来可能的操作是怎样的
//    //游戏信息记录了玩家获得的关键游戏信息，如果断线重连游戏信息是唯一需要被显示给玩家的信息
//    private boolean information = false;

    public OnwMessage(String message, ClientAction action, Object data) {
        this.message = message;
        this.action = action;
        this.data = data;
    }

    public ClientAction getAction() {
        return action;
    }

    public void setAction(ClientAction action) {
        this.action = action;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }

    public Date getDate() {
        return date;
    }

    @Override
    public String toString() {
        return "OnwMessage{" +
                "message='" + message + '\'' +
                ", action='" + action + '\'' +
                ", data=" + data +
                '}';
    }
}
