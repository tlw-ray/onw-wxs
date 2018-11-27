package com.xskr.onw.wxs.card;

import com.xskr.onw.wxs.core.Seat;
import com.xskr.onw.wxs.core.action.DataType;
import com.xskr.onw.wxs.rx.RxOnwRoom;

public class Tanner extends Card {
    @Override
    public String getDisplayName() {
        return "皮匠";
    }

    @Override
    public void start(RxOnwRoom room, Seat cardOwnerSeat) {
        super.start(room, cardOwnerSeat);
        processed = true;
    }

    @Override
    public void nightOperate(RxOnwRoom room, Seat cardOwnerSeat, DataType dataType, int id) {
        operated = true;
    }

    @Override
    public void nightProcess(RxOnwRoom room, Seat cardOwnerSeat) {

    }

    @Override
    public Card clone() {
        return new Tanner();
    }
}
