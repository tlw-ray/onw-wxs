package com.xskr.onw.wxs.core.card;

import com.xskr.onw.wxs.core.Room;
import com.xskr.onw.wxs.core.Seat;
import com.xskr.onw.wxs.core.action.DataType;
import com.xskr.onw.wxs.core.message.SeatMessage;

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
    public void start(Room room, Seat cardOwnerSeat){
        //初始化
        avatarCard = null;

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
    public void nightOperate(Room room, Seat cardOwnerSeat, DataType dataType, int id){
        if(avatarCard == null){
            if(dataType == DataType.SEAT){
                Seat seat = room.getSeats().get(id);
                if(seat != cardOwnerSeat){
                    avatarCard = seat.getCard();
                    avatarCard.start(room, cardOwnerSeat);
                }else{
                    //do nothing
                }
            }else{
                //do nothing
            }
        }else{
            avatarCard.nightOperate(room, cardOwnerSeat, dataType, id);
            nightOperateCompleted = avatarCard.isNightOperateCompleted();
        }
    }

    @Override
    public void nightProcess(Room room, Seat cardOwnerSeat) {
        avatarCard.nightProcess(room, cardOwnerSeat);
    }

    @Override
    public boolean isNightOperateCompleted() {
        if(avatarCard != null){
            return avatarCard.isNightOperateCompleted();
        }else{
            return false;
        }
    }

    //TODO 触发猎人技能

    @Override
    public Card clone() {
        //化身幽灵本身不会被克隆，无需实现该功能
        throw new RuntimeException("not implements");
    }

    public Card getAvatarCard() {
        return avatarCard;
    }
}
