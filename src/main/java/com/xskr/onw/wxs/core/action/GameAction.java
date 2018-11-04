package com.xskr.onw.wxs.core.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GameAction {

    //时间标签
    private Date date = new Date();
    private GameActionType gameActionType;
    private DataType dataType;
    private List<Integer> data = new ArrayList();
    private String message;

    public GameAction(GameActionType gameActionType) {
        this.gameActionType = gameActionType;
    }

    public GameAction(GameActionType gameActionType, DataType dataType) {
        this.gameActionType = gameActionType;
        this.dataType = dataType;
    }

    public GameActionType getGameActionType() {
        return gameActionType;
    }

    public void setGameActionType(GameActionType gameActionType) {
        this.gameActionType = gameActionType;
    }

    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    public List<Integer> getData() {
        return data;
    }

    public Date getDate() {
        return date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
