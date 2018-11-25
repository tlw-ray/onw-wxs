package com.xskr.onw.wxs.card;

import com.xskr.onw.wxs.core.Seat;
import com.xskr.onw.wxs.core.action.DataType;
import com.xskr.onw.wxs.core.message.SeatMessage;
import com.xskr.onw.wxs.rx.RxOnwRoom;

public class Doppelganger extends Card {

    private Card avatarCard;

    @Override
    public String getDisplayName() {
        return "化身幽灵";
    }

    /**
     * 操作提示
     * @param room
     * @param cardOwnerSeat
     */
    public void start(RxOnwRoom room, Seat cardOwnerSeat){
        //初始化
        avatarCard = null;
        canOperate = true;
        canProcess = false;
        operated = false;
        processed = false;

        //提示选择化身除自己之外的玩家身份
        String message = "选择一个玩家化身为他的身份";
        SeatMessage seatMessage = new SeatMessage(message);
        cardOwnerSeat.getInformation().add(seatMessage);
    }

    /**
     * 操作处理
     * @param room
     * @param dataType
     * @param id
     */
    public void nightOperate(RxOnwRoom room, Seat cardOwnerSeat, DataType dataType, int id){
        if(avatarCard == null){
            if(dataType == DataType.SEAT){
                Seat seat = room.getSeats().get(id);
                if(seat != cardOwnerSeat){
                    //处理化身操作
                    avatarCard = seat.getCard().clone();
                    avatarCard.listen(room);
                    avatarCard.start(room, cardOwnerSeat);
                    //触发化身事件
                    room.fireAvatarEvent();
                    //尝试触发所有操作都完成事件, 有可能化身操作是唯一需要的操作
                    room.attemptFireAllOperatedEvent();
                }else{
                    //do nothing
                }
            }else{
                //do nothing
            }
        }else{
            //化身后的身份行动
            avatarCard.nightOperate(room, cardOwnerSeat, dataType, id);
            operated = avatarCard.isOperated();
        }
    }

    @Override
    public void nightProcess(RxOnwRoom room, Seat cardOwnerSeat) {
        avatarCard.nightProcess(room, cardOwnerSeat);
    }

    @Override
    public boolean isOperated() {
        if(avatarCard != null){
            return avatarCard.isOperated();
        }else{
            return false;
        }
    }

    //TODO 触发化身猎人技能

    @Override
    public Card clone() {
        //化身幽灵本身不会被克隆，无需实现该功能
        throw new RuntimeException("not implements");
    }

    @Override
    public void unListen(RxOnwRoom room) {
        super.unListen(room);
        avatarCard.unListen(room);
    }

    public Card getAvatarCard() {
        return avatarCard;
    }
}
