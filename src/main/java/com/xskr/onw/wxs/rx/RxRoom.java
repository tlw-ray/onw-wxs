package com.xskr.onw.wxs.rx;

import com.xskr.onw.wxs.core.*;
import com.xskr.onw.wxs.card.*;
import com.xskr.onw.wxs.core.message.SeatMessage;
import com.xskr.onw.wxs.event.*;
import org.apache.commons.lang3.Range;

import javax.swing.event.EventListenerList;
import java.util.*;

public class RxRoom{

    //桌上三张牌
    public static final Range<Integer> DESKTOP_CARD_RANGE = Range.between(0, 2);
    //支持12个玩家
    public static final Range<Integer> PLAYER_RANGE = Range.between(0, 11);

    //房间号
    int id;

    // 该房间支持的所有卡牌被选中的状态
    protected boolean[] cardPicked = new boolean[Card.ROLE_COUNT];

    // 房间内的座位
    protected List<Seat> seats = new ArrayList();

    // 发牌后剩余的桌面3张牌垛
    protected Card[] desktopCards = new Card[3];

    //进入房间还但没有座位的玩家
    protected Set<WxUser> observers = new TreeSet();

    protected EventListenerList eventListeners = new EventListenerList();

    public RxRoom(int id){
        this.id = id;
    }

    public void sendMessage(WxUser user){

    }

    public void sendMessage(){

    }

    public void setMessage(SeatMessage seatMessage){
        for(Seat seat:seats){
            seat.getInformation().add(seatMessage);
        }
    }

    public Seat getSeatByWxUser(WxUser wxUser){
        for(Seat seat:seats){
            if(seat.getWxUser() == wxUser){
                return seat;
            }
        }
        return null;
    }

    public List<Seat> getSeats() {
        return seats;
    }

    public Card[] getDesktopCards() {
        return desktopCards;
    }

    public EventListenerList getEventListeners() {
        return eventListeners;
    }

    public int getId() {
        return id;
    }

    public boolean[] getCardPicked() {
        return cardPicked;
    }

    public Set<WxUser> getObservers() {
        return observers;
    }
}
