package com.xskr.onw.wxs.core.action;

public class GameAction {

    private GameActionType gameActionType;
    private DataType dataType;
    private int id;

    public GameAction(GameActionType gameActionType, DataType dataType, int id) {
        this.gameActionType = gameActionType;
        this.dataType = dataType;
        this.id = id;
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
