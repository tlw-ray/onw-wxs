package com.xskr.onw.wxs.card;

import com.xskr.onw.wxs.core.Seat;
import com.xskr.onw.wxs.core.action.DataType;
import com.xskr.onw.wxs.core.action.GameAction;
import com.xskr.onw.wxs.core.message.SeatMessage;
import com.xskr.onw.wxs.rx.RxOnwRoom;

import java.util.List;
// 为了能够并行卡牌被分为操作和行动两个步骤
// 操作表示用户进行的操作
// 行动表示应用该操作
public abstract class Card {

    public static final int ROLE_COUNT = 16;

    protected boolean canOperate = false;
    protected boolean canProcess = false;
    protected boolean operated = false;
    protected boolean processed = false;
    protected List<GameAction> actions;
    protected Seat cardOwnerSeat;

    public abstract String getDisplayName();

    public void start(RxOnwRoom room, Seat cardOwnerSeat){
        cardOwnerSeat.getInformation().clear();
        actions.clear();

        canOperate = false;
        operated = false;
        canProcess = false;
        processed = false;
        this.cardOwnerSeat = cardOwnerSeat;

        String message = "初始身份: " + getDisplayName();
        SeatMessage seatMessage = new SeatMessage(message);
        cardOwnerSeat.getInformation().add(seatMessage);
    }

    public abstract void nightOperate(RxOnwRoom room, Seat cardOwnerSeat, DataType dataType, int id);

    public abstract void nightProcess(RxOnwRoom room, Seat cardOwnerSeat);

    //投票后可能触发猎人技能的时机
    public void afterVote(){}

    //猎人在投票后行动后的结果
    public void afterVoteProcess(){}

    public boolean canOperate() {
        return canOperate;
    }

    public boolean isOperated() {
        return operated;
    }

    public abstract Card clone();

//    public void listen(RxOnwRoom room){
//        if(this instanceof AvatarListener){
//            room.getEventListeners().add(AvatarListener.class, (AvatarListener)this);
//        }else if(this instanceof AllOperatedListener){
//            room.getEventListeners().add(AllOperatedListener.class, (AllOperatedListener)this);
//        }else if(this instanceof AllProcessedListener){
//            room.getEventListeners().add(AllProcessedListener.class, (AllProcessedListener)this);
//        }else{
//            //unsupported listener type
//        }
//    }
//
//    public void unListen(RxOnwRoom room){
//        if(this instanceof AvatarListener){
//            room.getEventListeners().remove(AvatarListener.class, (AvatarListener)this);
//        }else if(this instanceof AllOperatedListener){
//            room.getEventListeners().remove(AllOperatedListener.class, (AllOperatedListener)this);
//        }else if(this instanceof AllProcessedListener){
//            room.getEventListeners().remove(AllProcessedListener.class, (AllProcessedListener)this);
//        }else{
//            //unsupported listener type
//        }
//    }


}
