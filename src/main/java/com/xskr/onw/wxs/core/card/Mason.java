package com.xskr.onw.wxs.core.card;

import com.xskr.onw.wxs.core.Room;
import com.xskr.onw.wxs.core.Seat;
import com.xskr.onw.wxs.core.action.DataType;
import com.xskr.onw.wxs.core.action.GameAction;
import com.xskr.onw.wxs.core.action.GameActionType;
import com.xskr.onw.wxs.core.message.SeatMessage;

import java.util.ArrayList;
import java.util.List;

public class Mason extends Card {

    @Override
    public String getDisplayName() {
        return "守夜人";
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
        List<Seat> partnerSeats = new ArrayList();
        for(int i=0;i<room.getAvailableSeatCount();i++){
            Seat seat = room.getSeats().get(i);
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
            message += room.getSeatTitle(seat);
        }
        SeatMessage seatMessage = new SeatMessage(message);
        cardOwnerSeat.getInformation().add(seatMessage);
    }

    @Override
    public Card clone() {
        return new Mason();
    }
}
