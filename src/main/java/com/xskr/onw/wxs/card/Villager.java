package com.xskr.onw.wxs.card;

import com.xskr.onw.wxs.core.Seat;
import com.xskr.onw.wxs.core.action.DataType;
import com.xskr.onw.wxs.rx.RxOnwRoom;

public class Villager extends Card {

    @Override
    public String getDisplayName() {
        return "村民";
    }

    @Override
    public void start(RxOnwRoom room, Seat cardOwnerSeat) {
        super.start(room, cardOwnerSeat);
        processed = true;
    }

    @Override
    public void nightOperate(RxOnwRoom room, Seat cardOwnerSeat, DataType dataType, int id) {

    }

    @Override
    public void nightProcess(RxOnwRoom room, Seat cardOwnerSeat) {

    }

    @Override
    public Card clone() {
        return new Villager();
    }
}
