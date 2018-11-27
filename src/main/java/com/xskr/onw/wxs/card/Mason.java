package com.xskr.onw.wxs.card;

import com.xskr.onw.wxs.core.Seat;
import com.xskr.onw.wxs.core.action.DataType;
import com.xskr.onw.wxs.core.message.SeatMessage;
import com.xskr.onw.wxs.rx.RxOnwRoom;

import java.util.ArrayList;
import java.util.List;

public class Mason extends Card{

    @Override
    public String getDisplayName() {
        return "守夜人";
    }

    @Override
    public void start(RxOnwRoom room, Seat cardOwnerSeat) {
        super.start(room, cardOwnerSeat);
        canProcess = true;
    }

    @Override
    public void nightOperate(RxOnwRoom room, Seat cardOwnerSeat, DataType dataType, int id) {
    }

    @Override
    public void nightProcess(RxOnwRoom room, Seat cardOwnerSeat) {
        List<Seat> partnerSeats = new ArrayList();
        for(Seat seat:room.getSeats()){
            if(seat.getCard().getClass() == Mason.class){
                partnerSeats.add(seat);
            }else if(seat.getCard().getClass() == Doppelganger.class){
                Doppelganger doppelganger = (Doppelganger)seat.getCard();
                if(doppelganger.getAvatarCard().getClass() == Mason.class){
                    partnerSeats.add(seat);
                }else{
                    //do nothing
                }
            }else {
                //do nothing
            }
        }

        String message = "守夜人是: ";
        for(Seat seat:partnerSeats){
            message += seat.getTitle();
        }
        SeatMessage seatMessage = new SeatMessage(message);
        cardOwnerSeat.getInformation().add(seatMessage);
        processed = true;
    }

    @Override
    public Card clone() {
        return new Mason();
    }
}
