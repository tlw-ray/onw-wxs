package com.xskr.onw.wxs.core.card;

import com.xskr.onw.wxs.core.Room;
import com.xskr.onw.wxs.core.Seat;
import com.xskr.onw.wxs.core.action.DataType;
import com.xskr.onw.wxs.core.message.SeatMessage;

public class Robber extends Card {

    Seat snatched;

    @Override
    public String getDisplayName() {
        return "强盗";
    }

    @Override
    public void start(Room room, Seat cardOwnerSeat) {
        snatched = null;
    }

    @Override
    public void nightOperate(Room room, Seat cardOwnerSeat, DataType dataType, int id) {
        if(dataType == DataType.SEAT && room.getSeats().get(id) != cardOwnerSeat){
            snatched = room.getSeats().get(id);
            nightOperateCompleted = true;
        }else{
            //无效的输入
        }
    }

    @Override
    public void nightProcess(Room room, Seat cardOwnerSeat) {
        Card tempCard = cardOwnerSeat.getCard();
        cardOwnerSeat.setCard(snatched.getCard());
        snatched.setCard(tempCard);

        String message = String.format("抢夺%s并与之交换身份为%s", room.getSeatTitle(snatched),
                cardOwnerSeat.getCard().getDisplayName());
        SeatMessage seatMessage = new SeatMessage(message);
        cardOwnerSeat.getInformation().add(seatMessage);
    }

    @Override
    public Card clone() {
        return new Robber();
    }
}
