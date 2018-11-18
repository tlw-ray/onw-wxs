package com.xskr.onw.wxs.core.card;

import com.xskr.onw.wxs.core.Room;
import com.xskr.onw.wxs.core.Seat;
import com.xskr.onw.wxs.core.action.DataType;

public class Hunter extends Card {
    @Override
    public String getDisplayName() {
        return "猎人";
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
        return new Hunter();
    }

    //投票后可能触发猎人技能的时机
    public void afterVote(Room room, Seat cardOwnerSeat){

    }

    //猎人在投票后行动后的结果
    public void afterVoteProcess(Room room, Seat cardOwnerSeat){

    }
}
