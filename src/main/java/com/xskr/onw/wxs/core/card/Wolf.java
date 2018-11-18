package com.xskr.onw.wxs.core.card;

import com.xskr.onw.wxs.core.action.GameAction;
import com.xskr.onw.wxs.core.action.GameActionType;
import com.xskr.onw.wxs.core.Room;
import com.xskr.onw.wxs.core.Seat;
import com.xskr.onw.wxs.core.action.DataType;
import com.xskr.onw.wxs.core.message.SeatMessage;

import java.util.ArrayList;
import java.util.List;

public class Wolf extends Card {

    private List<Seat> partnerSeats = new ArrayList();

    @Override
    public String getDisplayName() {
        return "狼人";
    }

    /**
     * 操作提示
     * @param room
     * @param cardOwnerSeat
     */
    public void start(Room room, Seat cardOwnerSeat){
        //初始化
        partnerSeats.clear();
        actions.clear();
        nightOperateCompleted = false;
    }

    @Override
    public void nightOperate(Room room, Seat cardOwnerSeat, DataType dataType, int id) {
        //查看是否有同伴
        for(int i=0;i<room.getAvailableSeatCount();i++){
            Seat seat = room.getSeats().get(i);
            if(seat.getCard().getClass() == Wolf.class){
                //发现同伴
                partnerSeats.add(seat);
            }else if(seat.getCard().getClass() == Doppelganger.class){
                Doppelganger doppelganger = (Doppelganger) seat.getCard();
                if(doppelganger.getAvatarCard().getClass() == Wolf.class){
                    //如果有化身幽灵
                    partnerSeats.add(seat);
                }
            }
        }
        if(partnerSeats.size() == 1){
            //孤狼
            String message = "请选择两张桌面牌,";
            SeatMessage seatMessage = new SeatMessage(message);
            cardOwnerSeat.getInformation().add(seatMessage);
        }else{
            //有同伴
            String message = "本局狼玩家是: ";
            for(Seat seat:partnerSeats){
                message += room.getSeatTitle(seat);
            }
            SeatMessage seatMessage = new SeatMessage(message);
            cardOwnerSeat.getInformation().add(seatMessage);
            nightOperateCompleted = true;
        }

        if(partnerSeats.size() == 1){
            //孤狼
            if(dataType == DataType.DESKTOP_CARD && id>=0 && id<3){
                if(actions.size() == 0){
                    GameAction singleWolfPick0 = new GameAction(GameActionType.PICK, dataType, id);
                    actions.add(singleWolfPick0);
                }else if(actions.size() == 1){
                    GameAction singleWolfPick1 = new GameAction(GameActionType.PICK, dataType, id);
                    actions.add(singleWolfPick1);
                    nightOperateCompleted = true;
                }else{
                    //do nothing
                }
            }else{
                // do nothing
            }
        }else{
            //do nothing
        }
    }

    /**
     * 执行操作
     * @param room
     */
    public void nightProcess(Room room, Seat cardOwnerSeat){
        if(partnerSeats.size() == 1){
            Card card0 = room.getDesktopCards().get(actions.get(0).getId());
            String message;
            if(card0.getClass() == Wolf.class){
                //如果是狼则再查看一张牌
                Card card1 = room.getDesktopCards().get(actions.get(1).getId());
                message = String.format("查看桌面第%s张是%s, 第%s是%s", actions.get(0).getId(), card0.getDisplayName(),
                        actions.get(1).getId(), card1.getDisplayName());
            }else{
                message = String.format("查看桌面第%s张是%s", actions.get(0).getId());
            }
            nightOperateCompleted = true;
            SeatMessage seatMessage = new SeatMessage(message);
            cardOwnerSeat.getInformation().add(seatMessage);
        }
    }

    @Override
    public Card clone() {
        Wolf wolf = new Wolf();
        return wolf;
    }
}
