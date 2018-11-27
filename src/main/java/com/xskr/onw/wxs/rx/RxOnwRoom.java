package com.xskr.onw.wxs.rx;

import com.xskr.onw.wxs.core.CardFactory;
import com.xskr.onw.wxs.core.Scene;
import com.xskr.onw.wxs.core.Seat;
import com.xskr.onw.wxs.core.WxUser;
import com.xskr.onw.wxs.card.*;
import com.xskr.onw.wxs.dealer.ActiveOnwDealer;
import com.xskr.onw.wxs.dealer.OnwDealer;
import com.xskr.onw.wxs.dealer.PrepareOnwDealer;
import com.xskr.onw.wxs.dealer.VoteOnwDealer;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

@Slf4j
public class RxOnwRoom extends RxRoom{

    //所有可用的卡牌
    public CardFactory cardFactory = new CardFactory();

//    //房间的状态
    protected Scene scene = Scene.PREPARE;

    protected PrepareOnwDealer prepareOnwDealer = new PrepareOnwDealer(this);
    protected ActiveOnwDealer activeOnwDealer = new ActiveOnwDealer(this);
    protected VoteOnwDealer voteOnwDealer = new VoteOnwDealer(this);

    //房间操作的执行者
    protected OnwDealer dealer = prepareOnwDealer;

    public RxOnwRoom(int id){
        super(id);
        //默认初始卡牌
        Card[] initializeCard = new Card[]{cardFactory.WOLF_0, cardFactory.MINION, cardFactory.MASON_0, cardFactory.MASON_1};
        for(Card card:initializeCard){
            int index = cardFactory.index(card);
            pickRoleCard(index);
        }
    }

    public synchronized void join(WxUser wxUser){
        dealer.join(wxUser);
    }

    public synchronized void leave(WxUser wxUser){
        dealer.leave(wxUser);
    }

    //玩家点击准备按钮
    public synchronized void pickReady(WxUser user){
        dealer.pickReady(user);
    }

    //玩家点击桌上牌
    public synchronized void pickDesktopCard(WxUser user, int id){
        dealer.pickDesktopCard(user, id);
    }

    //玩家点击座位
    public synchronized void pickSeat(WxUser user, int id){
        dealer.pickSeat(user, id);
    }

    //房主点击房间角色设定卡牌
    public synchronized void pickRoleCard(WxUser user, int id){
        dealer.pickRoleCard(user, id);
    }

    //点击角色设定卡牌
    public void pickRoleCard(int id){
        //改变座位状态
        cardPicked[id] = !cardPicked[id];
        //刷新座位数
        if(cardPicked[id]){
            //如果新选中一张卡牌则添加一个座位
            seats.add(new Seat(seats.size()));
        }else{
            //否则减少最后一个座位
            seats.remove(seats.size() - 1);
        }
    }

    public void attemptFireAllOperatedEvent(){
        boolean canFire = true;
        for (Seat seat : seats) {
            if (!seat.getCard().isOperated()) {
                canFire = false;
                break;
            }
        }
        if(canFire){
            Set<Seat> seatSet = new HashSet(getSeats());
            Set<Seat> seatOperated = new HashSet();
            //执行所有Process
            for(Card card:cardFactory.CARDS){
                for(Seat seat:seatSet){
                    if(seat.getCard() == card){
                        seatOperated.add(seat);
                        seat.getCard().nightProcess(this, seat);
                    }else{
                        //do nothing
                    }
                }
                //移除行动过的座位提升效率
                seatSet.removeAll(seatOperated);
            }

            //卡牌执行结束时触发进入投票阶段
            setScene(Scene.VOTE);
        }
    }

    public Scene getScene() {
        return scene;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
        if(scene == Scene.PREPARE){
            this.dealer = prepareOnwDealer;
            prepareOnwDealer.initialize();
        }else if(scene == Scene.ACTIVATE){
            this.dealer = activeOnwDealer;
            activeOnwDealer.initialize();
        }else if(scene == Scene.VOTE){
            this.dealer = voteOnwDealer;
            voteOnwDealer.initialize();
        }else{
            //do nothing
        }
    }
}
