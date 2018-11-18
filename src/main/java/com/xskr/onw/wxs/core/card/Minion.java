package com.xskr.onw.wxs.core.card;

import com.xskr.onw.wxs.core.Room;
import com.xskr.onw.wxs.core.Seat;
import com.xskr.onw.wxs.core.action.DataType;
import com.xskr.onw.wxs.core.message.SeatMessage;

import java.util.ArrayList;
import java.util.List;

public class Minion extends Card {

    @Override
    public String getDisplayName() {
        return "爪牙";
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
        List<Seat> wolfSeats = new ArrayList();
        for(int i=0;i<room.getAvailableSeatCount();i++){
            Seat seat = room.getSeats().get(i);
            Card currentCard = seat.getCard();
            if(currentCard.getClass() == Wolf.class){
                wolfSeats.add(seat);
            }else if(currentCard.getClass() == Doppelganger.class){
                Doppelganger doppelganger = (Doppelganger)currentCard;
                if(doppelganger.getAvatarCard().getClass() == Wolf.class){
                    wolfSeats.add(seat);
                }else{
                    //do nothing
                }
            }else{
                //do nothing
            }
        }

        String message;
        if(wolfSeats.size() == 0){
            message = "场面上没有狼人";
        }else{
            message = "狼人是: ";
            for(Seat seat:wolfSeats){
                message += room.getSeatTitle(seat);
            }
        }
        SeatMessage seatMessage = new SeatMessage(message);
        cardOwnerSeat.getInformation().add(seatMessage);
    }

    @Override
    public Card clone() {
        return null;
    }
}
