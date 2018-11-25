package com.xskr.onw.wxs.card;

import com.xskr.onw.wxs.core.Seat;
import com.xskr.onw.wxs.core.action.DataType;
import com.xskr.onw.wxs.core.message.SeatMessage;
import com.xskr.onw.wxs.rx.RxOnwRoom;

public class Troublemaker extends Card {

    private Integer seatID0, seatID1;

    @Override
    public String getDisplayName() {
        return "捣蛋鬼";
    }

    @Override
    public void start(RxOnwRoom room, Seat cardOwnerSeat) {
        seatID0 = null;
        seatID1 = null;
    }

    @Override
    public void nightOperate(RxOnwRoom room, Seat cardOwnerSeat, DataType dataType, int id) {
        if(dataType == DataType.SEAT && id<room.getSeats().size() && room.getSeats().get(id) != cardOwnerSeat){
            if(seatID0 == null){
                seatID0 = id;
            }else{
                seatID1 = id;
                operated = true;
            }
        }
    }

    @Override
    public void nightProcess(RxOnwRoom room, Seat cardOwnerSeat) {
        Seat seat0 = room.getSeats().get(seatID0);
        Seat seat1 = room.getSeats().get(seatID1);
        Card card0 = seat0.getCard();
        seat0.setCard(seat1.getCard());
        seat1.setCard(card0);
        String message = String.format("交换了%s和%s的身份", seat0.getTitle(), seat1.getTitle());
        SeatMessage seatMessage = new SeatMessage(message);
        cardOwnerSeat.getInformation().add(seatMessage);
    }

    @Override
    public Card clone() {
        return new Troublemaker();
    }
}
