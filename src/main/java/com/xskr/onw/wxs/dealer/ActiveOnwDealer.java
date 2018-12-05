package com.xskr.onw.wxs.dealer;

import com.xskr.onw.wxs.core.Seat;
import com.xskr.onw.wxs.core.WxUser;
import com.xskr.onw.wxs.core.action.DataType;
import com.xskr.onw.wxs.card.*;
import com.xskr.onw.wxs.rx.RxOnwRoom;

import java.util.HashSet;
import java.util.Set;

//封装所有游戏行动过程中的操作
public class ActiveOnwDealer extends GamingOnwDealer{

    //开始后可以进行行动的卡牌
    private static Set<Class<? extends Card>> firstOperateCardClasses = new HashSet();

    static{
        //游戏开始后可以立即行动的卡牌
        firstOperateCardClasses.add(Doppelganger.class);
        firstOperateCardClasses.add(Seer.class);
        firstOperateCardClasses.add(Robber.class);
        firstOperateCardClasses.add(Troublemaker.class);
        firstOperateCardClasses.add(Drunk.class);
    }

    public ActiveOnwDealer(RxOnwRoom room) {
        super(room);
    }

    public synchronized void initialize(){
        //初始化时所有座位上可以立即操作的卡牌向客户端发送操作提示
        for(Seat seat:room.getSeats()){
            Card card = seat.getCard();
            if(firstOperateCardClasses.contains(card.getClass())){
                card.start(room, seat);
            }
        }

        //尝试触发一次所有操作完成和所有处理完成,可能存在没有任何操作和处理需要执行的情况
        room.attemptFireAllOperatedEvent();

        //发送立即可操作卡牌的消息
        room.sendMessage();
    }

    @Override
    public void auto() {

    }

    @Override
    public synchronized void leave(WxUser user) {
        //行动状态下离开房间, 设定座位上的玩家为空,
        super.leave(user);
        //TODO 并在所有投票结束时玩家执行随机操作
    }

    @Override
    public synchronized void pickDesktopCard(WxUser user, int id) {
        //行动状态下点击桌面卡牌, 由具体卡牌处理该操作
        Seat seat = room.getSeats().get(id);
        Card card = seat.getCard();
        if(card.canOperate() && !card.isOperated()) {
            card.nightOperate(room, seat, DataType.DESKTOP_CARD, id);
            if(card.isOperated()){
                room.attemptFireAllOperatedEvent();
            }
        }
    }

    @Override
    public synchronized void pickReady(WxUser user) {
        //行动状态下点击准备不会有任何效果
    }

    @Override
    public synchronized void pickRoleCard(WxUser user, int id) {
        //行动状态下点击角色设定不会有任何效果
    }

    @Override
    public synchronized void pickSeat(WxUser user, int id) {
        //行动状态下点击座位,由具体卡牌处理该操作
        Seat seat = room.getSeats().get(id);
        Card card = seat.getCard();
        if(card.canOperate() && !card.isOperated()) {
            card.nightOperate(room, seat, DataType.SEAT, id);
            if(card.isOperated()){
                room.attemptFireAllOperatedEvent();
            }
        }
    }
}
