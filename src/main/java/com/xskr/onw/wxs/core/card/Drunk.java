package com.xskr.onw.wxs.core.card;

import com.xskr.onw.wxs.core.Room;
import com.xskr.onw.wxs.core.Seat;
import com.xskr.onw.wxs.core.action.DataType;
import com.xskr.onw.wxs.core.action.GameAction;
import com.xskr.onw.wxs.core.action.GameActionType;
import com.xskr.onw.wxs.core.message.SeatMessage;

public class Drunk extends Card {

    @Override
    public String getDisplayName() {
        return "酒鬼";
    }

    @Override
    public void start(Room room, Seat cardOwnerSeat) {
        actions.clear();

        String message = "请选择桌上一张牌与之交换";
        SeatMessage seatMessage = new SeatMessage(message);
        cardOwnerSeat.getInformation().add(seatMessage);
    }

    @Override
    public void nightOperate(Room room, Seat cardOwnerSeat, DataType dataType, int id) {
        if(dataType == DataType.DESKTOP_CARD){
            GameAction exchangeDesktopCard = new GameAction(GameActionType.EXCHANGE, DataType.DESKTOP_CARD, id);
            actions.add(exchangeDesktopCard);
            nightOperateCompleted = true;
        }else{
            //do nothing
        }
    }

    @Override
    public void nightProcess(Room room, Seat cardOwnerSeat) {
        GameAction exchangeDesktopCard = actions.get(0);
        Card card = room.getDesktopCards().get(exchangeDesktopCard.getId());
        cardOwnerSeat.setCard(card);
        String message = String.format("与桌上第%s张牌交换", exchangeDesktopCard.getId());
        SeatMessage seatMessage = new SeatMessage(message);
        cardOwnerSeat.getInformation().add(seatMessage);
    }

    @Override
    public Drunk clone() {
        Drunk drunk = new Drunk();
        return drunk;
    }


}
