package com.xskr.onw.wxs.core.message;

import com.xskr.onw.wxs.core.action.GameAction;

public class SeatMessage extends AbsMessage{
    protected String message;
    protected GameAction[] gameActions;

    public SeatMessage(String message, GameAction... gameActions) {
        this.message = message;
        this.gameActions = gameActions;
    }

    public String getMessage() {
        return message;
    }

    public GameAction[] getGameActions() {
        return gameActions;
    }
}
