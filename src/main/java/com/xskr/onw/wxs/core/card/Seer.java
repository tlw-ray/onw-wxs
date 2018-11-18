package com.xskr.onw.wxs.core.card;

import com.xskr.onw.wxs.core.Room;
import com.xskr.onw.wxs.core.Seat;
import com.xskr.onw.wxs.core.action.DataType;
import com.xskr.onw.wxs.core.action.GameAction;
import com.xskr.onw.wxs.core.action.GameActionType;
import com.xskr.onw.wxs.core.message.SeatMessage;
import org.apache.commons.lang3.Range;

public class Seer extends Card {

    Integer pickDesktop0;
    Integer pickDesktop1;
    Integer pickSeat;

    @Override
    public String getDisplayName() {
        return "预言家";
    }

    @Override
    public void start(Room room, Seat cardOwnerSeat) {
        pickDesktop0 = null;
        pickDesktop1 = null;
        pickSeat = null;
    }

    @Override
    public void nightOperate(Room room, Seat cardOwnerSeat, DataType dataType, int id) {
        //如果还没有选择过牌
        if(pickDesktop0 == null) {
            if (dataType == DataType.DESKTOP_CARD && Room.DESKTOP_CARD_RANGE.contains(id)) {
                pickDesktop0 = id;
            } else if (dataType == DataType.SEAT && id>=0 && id<room.getAvailableSeatCount()) {
                pickSeat = id;
                nightOperateCompleted = true;
            }else{
                // do nothing
            }
        }else{
            if(dataType == DataType.DESKTOP_CARD && Room.DESKTOP_CARD_RANGE.contains(id) && id != pickDesktop0){
                //对查阅的卡牌进行排序，便于后面输出后查阅
                if(id < pickDesktop0){
                    pickDesktop1 = pickDesktop0;
                    pickDesktop0 = id;
                }else{
                    pickDesktop1 = id;
                }
                nightOperateCompleted = true;
            }else{
                // do nothing
            }
        }
    }

    @Override
    public void nightProcess(Room room, Seat cardOwnerSeat) {
        String message;
        if(pickSeat != null){
            Seat seat = room.getSeats().get(pickSeat);
            message = String.format("查看%s的身份是%s", room.getSeatTitle(seat), seat.getCard().getDisplayName());
        }else{
            String card0 = room.getDesktopCards().get(pickDesktop0).getDisplayName();
            String card1 = room.getDesktopCards().get(pickDesktop1).getDisplayName();
            message = String.format("查看桌上卡牌第%s张%s,第%s张是%s", pickDesktop0, card0, pickDesktop1, card1);
        }
        SeatMessage seatMessage = new SeatMessage(message);
        cardOwnerSeat.getInformation().add(seatMessage);
    }

    @Override
    public Card clone() {
        return new Seer();
    }
}
