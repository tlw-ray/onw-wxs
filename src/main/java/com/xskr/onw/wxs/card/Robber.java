package com.xskr.onw.wxs.card;

import com.xskr.onw.wxs.core.Seat;
import com.xskr.onw.wxs.core.action.DataType;
import com.xskr.onw.wxs.core.message.SeatMessage;
import com.xskr.onw.wxs.rx.RxOnwRoom;

public class Robber extends Card {

    private Seat snatched;

    @Override
    public String getDisplayName() {
        return "强盗";
    }

    @Override
    public void start(RxOnwRoom room, Seat cardOwnerSeat) {
        super.start(room, cardOwnerSeat);
        snatched = null;
        canOperate = true;
    }

    @Override
    public void nightOperate(RxOnwRoom room, Seat cardOwnerSeat, DataType dataType, int id) {
        if(dataType == DataType.SEAT && room.getSeats().get(id) != cardOwnerSeat && snatched == null){
            snatched = room.getSeats().get(id);
            operated = true;
            canProcess = true;
        }else{
            //无效的输入
        }
    }

    @Override
    public void nightProcess(RxOnwRoom room, Seat cardOwnerSeat) {
        Card tempCard = cardOwnerSeat.getCard();
        cardOwnerSeat.setCard(snatched.getCard());
        snatched.setCard(tempCard);

        String message = String.format("抢夺%s并与之交换身份为%s", snatched.getTitle(),
                cardOwnerSeat.getCard().getDisplayName());
        SeatMessage seatMessage = new SeatMessage(message);
        cardOwnerSeat.getInformation().add(seatMessage);

        processed = true;
    }

    @Override
    public Card clone() {
        return new Robber();
    }
}
