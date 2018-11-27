package com.xskr.onw.wxs.card;

import com.xskr.onw.wxs.core.Seat;
import com.xskr.onw.wxs.core.action.DataType;
import com.xskr.onw.wxs.core.message.SeatMessage;
import com.xskr.onw.wxs.rx.RxOnwRoom;

public class Insomniac extends Card{

    private boolean checked;

    @Override
    public String getDisplayName() {
        return "失眠者";
    }

    @Override
    public void start(RxOnwRoom room, Seat cardOwnerSeat) {
        super.start(room, cardOwnerSeat);
        checked = false;
        canProcess = true;
        canOperate = true;
        operated = true;
    }

    @Override
    public void nightOperate(RxOnwRoom room, Seat cardOwnerSeat, DataType dataType, int id) {

    }

    @Override
    public void nightProcess(RxOnwRoom room, Seat cardOwnerSeat) {
        String message;
        if(cardOwnerSeat.getCard() == this){
            message = "身份未被换过: ";
        }else{
            message = "身份被换为: ";
        }
        message += cardOwnerSeat.getCard().getDisplayName();
        SeatMessage seatMessage = new SeatMessage(message);
        cardOwnerSeat.getInformation().add(seatMessage);
        this.checked = true;
        processed = true;
    }

    public boolean isChecked() {
        return checked;
    }

    @Override
    public Card clone() {
        return new Insomniac();
    }

}
