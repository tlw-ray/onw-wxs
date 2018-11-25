package com.xskr.onw.wxs.card;

import com.xskr.onw.wxs.core.Seat;
import com.xskr.onw.wxs.core.action.DataType;
import com.xskr.onw.wxs.rx.RxOnwRoom;

public class Hunter extends Card {
    @Override
    public String getDisplayName() {
        return "猎人";
    }

    @Override
    public void start(RxOnwRoom room, Seat cardOwnerSeat) {

    }

    @Override
    public void nightOperate(RxOnwRoom room, Seat cardOwnerSeat, DataType dataType, int id) {
        this.operated = true;
    }

    @Override
    public void nightProcess(RxOnwRoom room, Seat cardOwnerSeat) {
        this.processed = true;
    }

    @Override
    public Card clone() {
        return new Hunter();
    }

    //投票后可能触发猎人技能的时机
    public void afterVote(RxOnwRoom room, Seat cardOwnerSeat){

    }

    //猎人在投票后行动后的结果
    public void afterVoteProcess(RxOnwRoom room, Seat cardOwnerSeat){

    }
}
