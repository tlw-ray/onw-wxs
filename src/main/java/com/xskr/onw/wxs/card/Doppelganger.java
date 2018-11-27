package com.xskr.onw.wxs.card;

import com.xskr.onw.wxs.core.Seat;
import com.xskr.onw.wxs.core.action.DataType;
import com.xskr.onw.wxs.core.message.SeatMessage;
import com.xskr.onw.wxs.rx.RxOnwRoom;

import java.util.ArrayList;
import java.util.List;

public class Doppelganger extends Card {

    private static final List<Class<? extends Card>> operateAfterAvatar = new ArrayList();

    static{
        //TODO 化身幽灵自身的行动处理流程
        operateAfterAvatar.add(Wolf.class);
        operateAfterAvatar.add(Minion.class);
        operateAfterAvatar.add(Mason.class);
    }

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
        super.start(room, cardOwnerSeat);
        //初始化
        avatarCard = null;
        canOperate = true;

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
                    //触发化身事件
                    fireAvatarEvent(room);
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
            canOperate = avatarCard.canOperate();
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

    private void fireAvatarEvent(RxOnwRoom room){
        //被化身牌触发行动
        avatarCard.start(room, cardOwnerSeat);
        for(Seat seat:room.getSeats()){
            Card card = seat.getCard();
            if(operateAfterAvatar.contains(card.getClass())){
                card.start(room, cardOwnerSeat);
            }
        }
        //尝试触发所有操作都完成事件
        room.attemptFireAllOperatedEvent();
    }

    public Card getAvatarCard() {
        return avatarCard;
    }
}
