package com.xskr.onw.wxs.core.card;

import com.xskr.onw.wxs.core.Room;
import com.xskr.onw.wxs.core.Seat;
import com.xskr.onw.wxs.core.action.DataType;

public class Tanner extends Card {
    @Override
    public String getDisplayName() {
        return "皮匠";
    }

    @Override
    public void start(Room room, Seat cardOwnerSeat) {

    }

    @Override
    public void nightOperate(Room room, Seat cardOwnerSeat, DataType dataType, int id) {
        nightOperateCompleted = true;
    }

    @Override
    public void nightProcess(Room room, Seat cardOwnerSeat) {

    }

    @Override
    public Card clone() {
        return new Tanner();
    }
}
