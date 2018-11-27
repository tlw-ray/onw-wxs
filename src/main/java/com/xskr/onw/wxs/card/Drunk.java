package com.xskr.onw.wxs.card;

import com.xskr.onw.wxs.core.Seat;
import com.xskr.onw.wxs.core.action.DataType;
import com.xskr.onw.wxs.core.action.GameAction;
import com.xskr.onw.wxs.core.action.GameActionType;
import com.xskr.onw.wxs.core.message.SeatMessage;
import com.xskr.onw.wxs.rx.RxOnwRoom;

public class Drunk extends Card {

    @Override
    public String getDisplayName() {
        return "酒鬼";
    }

    @Override
    public void start(RxOnwRoom room, Seat cardOwnerSeat) {
        super.start(room, cardOwnerSeat);

        actions.clear();
        canOperate = true;

        String message = "请选择桌上一张牌与之交换";
        SeatMessage seatMessage = new SeatMessage(message);
        cardOwnerSeat.getInformation().add(seatMessage);
    }

    @Override
    public void nightOperate(RxOnwRoom room, Seat cardOwnerSeat, DataType dataType, int id) {
        if(dataType == DataType.DESKTOP_CARD){
            GameAction exchangeDesktopCard = new GameAction(GameActionType.EXCHANGE, DataType.DESKTOP_CARD, id);
            actions.add(exchangeDesktopCard);
            operated = true;
            room.attemptFireAllOperatedEvent();
            canProcess = true;
        }else{
            //do nothing
        }
    }

    @Override
    public void nightProcess(RxOnwRoom room, Seat cardOwnerSeat) {
        GameAction exchangeDesktopCard = actions.get(0);
        Card desktopCard = room.getDesktopCards()[exchangeDesktopCard.getId()];
        room.getDesktopCards()[exchangeDesktopCard.getId()] = cardOwnerSeat.getCard();
        cardOwnerSeat.setCard(desktopCard);
        String message = String.format("与桌上第%s张牌交换", exchangeDesktopCard.getId());
        SeatMessage seatMessage = new SeatMessage(message);
        cardOwnerSeat.getInformation().add(seatMessage);
        processed = true;
    }

    @Override
    public Drunk clone() {
        Drunk drunk = new Drunk();
        return drunk;
    }


}
