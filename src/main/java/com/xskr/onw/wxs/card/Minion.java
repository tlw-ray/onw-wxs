package com.xskr.onw.wxs.card;

import com.xskr.onw.wxs.core.Seat;
import com.xskr.onw.wxs.core.action.DataType;
import com.xskr.onw.wxs.event.AvatarListener;
import com.xskr.onw.wxs.core.message.SeatMessage;
import com.xskr.onw.wxs.rx.RxOnwRoom;

import java.util.ArrayList;
import java.util.List;

public class Minion extends Card implements AvatarListener {

    @Override
    public String getDisplayName() {
        return "爪牙";
    }

    @Override
    public void start(RxOnwRoom room, Seat cardOwnerSeat) {

    }

    @Override
    public void nightOperate(RxOnwRoom room, Seat cardOwnerSeat, DataType dataType, int id) {
        operated = true;
    }

    @Override
    public void nightProcess(RxOnwRoom room, Seat cardOwnerSeat) {
    }

    @Override
    public Card clone() {
        return null;
    }

    @Override
    public void afterAvatar(RxOnwRoom rxRoom) {
        List<Seat> wolfSeats = new ArrayList();
        for(Seat seat:rxRoom.getSeats()){
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
                message += seat.getTitle();
            }
        }
        SeatMessage seatMessage = new SeatMessage(message);
        cardOwnerSeat.getInformation().add(seatMessage);
        processed = true;
    }
}
