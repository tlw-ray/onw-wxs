package com.xskr.onw.wxs.core.card;

import com.xskr.onw.wxs.core.Room;
import com.xskr.onw.wxs.core.Seat;
import com.xskr.onw.wxs.core.action.DataType;
import com.xskr.onw.wxs.core.message.SeatMessage;

public class Insomniac extends Card {
    @Override
    public String getDisplayName() {
        return "失眠者";
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
        String message;
        if(cardOwnerSeat.getCard() == this){
            message = "身份未被换过: ";
        }else{
            message = "身份被换为: ";
        }
        message += cardOwnerSeat.getCard().getDisplayName();
        SeatMessage seatMessage = new SeatMessage(message);
        cardOwnerSeat.getInformation().add(seatMessage);
    }

    @Override
    public Card clone() {
        return new Insomniac();
    }
}
