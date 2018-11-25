package com.xskr.onw.wxs.card;

import com.xskr.onw.wxs.core.action.GameAction;
import com.xskr.onw.wxs.core.action.GameActionType;
import com.xskr.onw.wxs.core.Seat;
import com.xskr.onw.wxs.core.action.DataType;
import com.xskr.onw.wxs.event.AvatarListener;
import com.xskr.onw.wxs.core.message.SeatMessage;
import com.xskr.onw.wxs.rx.RxOnwRoom;

import java.util.ArrayList;
import java.util.List;

public class Wolf extends Card implements AvatarListener {

    private List<Seat> partnerSeats = new ArrayList();
    private Seat cardOwnerSeat;

    @Override
    public String getDisplayName() {
        return "狼人";
    }

    /**
     * 操作提示
     * @param room
     * @param cardOwnerSeat
     */
    public void start(RxOnwRoom room, Seat cardOwnerSeat){
        //初始化
        partnerSeats.clear();
        actions.clear();
        operated = false;
        this.cardOwnerSeat = cardOwnerSeat;
    }

    @Override
    public void nightOperate(RxOnwRoom room, Seat cardOwnerSeat, DataType dataType, int id) {
        if (partnerSeats.size() == 1) {
            //孤狼
            if (dataType == DataType.DESKTOP_CARD && id >= 0 && id < 3) {
                if (actions.size() == 0) {
                    GameAction singleWolfPick0 = new GameAction(GameActionType.PICK, dataType, id);
                    actions.add(singleWolfPick0);
                } else if (actions.size() == 1) {
                    GameAction singleWolfPick1 = new GameAction(GameActionType.PICK, dataType, id);
                    actions.add(singleWolfPick1);
                    operated = true;
                } else {
                    //do nothing
                }
            } else {
                // do nothing
            }
        } else {
            //do nothing
        }
    }

    /**
     * 执行操作
     * @param room
     */
    public void nightProcess(RxOnwRoom room, Seat cardOwnerSeat){
        if(partnerSeats.size() == 1){
            Card card0 = room.getDesktopCards()[actions.get(0).getId()];
            String message;
            if(card0.getClass() == Wolf.class){
                //如果是狼则再查看一张牌
                Card card1 = room.getDesktopCards()[actions.get(1).getId()];
                message = String.format("查看桌面第%s张是%s, 第%s是%s", actions.get(0).getId(), card0.getDisplayName(),
                        actions.get(1).getId(), card1.getDisplayName());
            }else{
                message = String.format("查看桌面第%s张是%s", actions.get(0).getId());
            }
            operated = true;
            SeatMessage seatMessage = new SeatMessage(message);
            cardOwnerSeat.getInformation().add(seatMessage);
        }
    }

    @Override
    public Card clone() {
        Wolf wolf = new Wolf();
        return wolf;
    }

    @Override
    public void afterAvatar(RxOnwRoom rxRoom) {
        //查看是否有同伴
        for (Seat seat : rxRoom.getSeats()) {
            if (seat.getCard().getClass() == Wolf.class) {
                //发现同伴
                partnerSeats.add(seat);
            } else if (seat.getCard().getClass() == Doppelganger.class) {
                Doppelganger doppelganger = (Doppelganger) seat.getCard();
                if (doppelganger.getAvatarCard().getClass() == Wolf.class) {
                    //如果有化身幽灵
                    partnerSeats.add(seat);
                }
            }
        }
        if (partnerSeats.size() == 1) {
            //孤狼
            String message = "请选择两张桌面牌,";
            SeatMessage seatMessage = new SeatMessage(message);
            cardOwnerSeat.getInformation().add(seatMessage);
        } else {
            //有同伴
            String message = "本局狼玩家是: ";
            for (Seat seat : partnerSeats) {
                message += seat.getTitle();
            }
            SeatMessage seatMessage = new SeatMessage(message);
            cardOwnerSeat.getInformation().add(seatMessage);
            operated = true;
        }
    }
}
