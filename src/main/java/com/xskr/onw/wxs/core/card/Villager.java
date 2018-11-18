package com.xskr.onw.wxs.core.card;

import com.xskr.onw.wxs.core.Room;
import com.xskr.onw.wxs.core.Seat;
import com.xskr.onw.wxs.core.action.DataType;

public class Villager extends Card {

    @Override
    public String getDisplayName() {
        return "村民";
    }

    @Override
    public void start(Room room, Seat cardOwnerSeat) {

    }

    @Override
    public void nightOperate(Room room, Seat cardOwnerSeat, DataType dataType, int id) {

    }

    @Override
    public void nightProcess(Room room, Seat cardOwnerSeat) {

    }

    @Override
    public Card clone() {
        return new Villager();
    }
}
