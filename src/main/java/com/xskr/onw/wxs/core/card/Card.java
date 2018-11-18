package com.xskr.onw.wxs.core.card;

import com.xskr.onw.wxs.core.Room;
import com.xskr.onw.wxs.core.Seat;
import com.xskr.onw.wxs.core.action.DataType;
import com.xskr.onw.wxs.core.action.GameAction;
import com.xskr.onw.wxs.core.message.SeatMessage;

import java.util.List;
import java.util.Objects;

public abstract class Card {

    public static final int ROLE_COUNT = 16;

    protected boolean nightOperateCompleted = true;
    protected List<GameAction> actions;

    public abstract String getDisplayName();

    public void identity(Seat cardOwnerSeat){
        String message = "初始身份: " + getDisplayName();
        SeatMessage seatMessage = new SeatMessage(message);
        cardOwnerSeat.getInformation().add(seatMessage);
        nightOperateCompleted = false;
    }

    public abstract void start(Room room, Seat cardOwnerSeat);

    public abstract void nightOperate(Room room, Seat cardOwnerSeat, DataType dataType, int id);

    public abstract void nightProcess(Room room, Seat cardOwnerSeat);

    //投票后可能触发猎人技能的时机
    public void afterVote(){}

    //猎人在投票后行动后的结果
    public void afterVoteProcess(){}

    public boolean isNightOperateCompleted() {
        return nightOperateCompleted;
    }

    public abstract Card clone();
}
