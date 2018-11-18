package com.xskr.onw.wxs.rx;

import com.alibaba.fastjson.JSON;
import com.xskr.onw.wxs.core.*;
import com.xskr.onw.wxs.core.card.Card;
import com.xskr.onw.wxs.core.message.OnwMessage;
import com.xskr.onw.wxs.core.message.RoomMessage;
import io.reactivex.Observable;
import org.apache.commons.lang3.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class RxRoom {

    //桌上三张牌
    public static final Range<Integer> DESKTOP_CARD_RANGE = Range.between(0, 2);
    //支持12个玩家
    public static final Range<Integer> PLAYER_RANGE = Range.between(0, 11);

    private Logger logger = LoggerFactory.getLogger(getClass());

    //所有可用的卡牌
    public CardFactory cardFactory = new CardFactory();

    // 该房间支持的所有卡牌被选中的状态
    private boolean[] cardPicked = new boolean[Card.ROLE_COUNT];

    // 房间内的座位
    private List<Seat> seats = new ArrayList();
    // 发牌后剩余的桌面3张牌垛
    private Card[] desktopCards = new Card[3];

    //进入房间还但没有座位的玩家
    private Set<WxUser> observers = new TreeSet();

    //房间的状态
    private Scene scene = Scene.PREPARE;

    //房间是否改变了
    private boolean roomChanged = false;

    public RxRoom(){
        //默认初始卡牌
        Card[] initializeCard = new Card[]{cardFactory.WOLF_0, cardFactory.MINION, cardFactory.MASON_0, cardFactory.MASON_1};
        for(Card card:initializeCard){
            int index = cardFactory.index(card);
            pickRoleCard(index);
        }
    }

    public synchronized void join(WxUser wxUser){
        String openid = wxUser.getOpenid();
        //游戏在不同状态有人加入
        if(scene == Scene.PREPARE){
            //加入准备状态的房间,默认行为是找一个空座位坐下
            boolean seated = false;
            for(int i=0;i<seats.size();i++){
                Seat seat = seats.get(i);
                if(seat.getWxUser() == null){
                    seat.setWxUser(wxUser);
                    seated = true;
                    break;
                }else{
                    //do nothing
                }
            }
            //如果没有空座位了就加入observer
            if(!seated){
                observers.add(wxUser);
                //房间变化了, 但无需立即通知所有人
                roomChanged = true;
                //需要立即通知该玩家房间内的情况
                sendMessage(openid);
            }else{
                //立即通知所有人有新玩家加入
                sendMessage();
            }
        }else{
            //游戏在进行中有人加入
            Seat oldUserSeat = null;
            for(int i=0;i<seats.size();i++){
                Seat seat = seats.get(i);
                WxUser user = seat.getWxUser();
                if((user.getOpenid()==null || user.getOpenid() == openid)
                        && seat.getOldUserName().equals(openid)){
                    oldUserSeat = seat;
                }
            }

            if(oldUserSeat != null){
                //玩家之前从座位上离开了，现在回到了座位，断线重连
                oldUserSeat.setWxUser(wxUser);
                //立即通知该玩家房间内的情况
                sendMessage(openid);
            }else{
                //加入observer
                observers.add(wxUser);
                //房间加入了新的Observer导致状态改变，但无需立即通知所有人
                roomChanged = true;
            }
        }
    }

    public synchronized void leave(String openid){
        WxUser wxUser = new WxUser(openid, null, null);
        //从观看者中移除
        if(observers.remove(wxUser)){
            //房间改变了但无需立即发送消息给所有人
            roomChanged = true;
        }else{
            //从座位上移除该玩家
            Seat playerSeat = getSeatByWxUser(wxUser);
            if(playerSeat != null) {
                playerSeat.setWxUser(null);
                //房间变化了立即发送消息给所有人
                sendMessage();
            }else{
                String message = String.format("房间内不存在玩家%s.", wxUser);
                logger.error(message);
            }
        }
    }

    public void switchReady(String openid){
        WxUser wxUser = new WxUser(openid, null, null);
        if(scene == Scene.ACTIVATE || scene == Scene.VOTE) {
            //如果已经开始或正在投票则不可改变ready状态
        }else if(scene == Scene.PREPARE){
            Seat seat = getSeatByWxUser(wxUser);
            if(seat != null) {
                //玩家准备状态改变
                pickReady(seat);
            }else{
                if(observers.contains(openid)){
                    String message = String.format("玩家%s是观看者，无法设定准备状态。", openid);
                    throw new RuntimeException(message);
                }else{
                    String message = String.format("玩家%s不在该房间，无法设定准备状态。", openid);
                    throw new RuntimeException(message);
                }
            }
        }else{
            //TODO 客户端应根据场景控制Ready按钮可用性，禁止用户发出此请求
            throw new RuntimeException("未支持的场景: " + scene);
        }
    }

    public void pickRoleCard(String openid, int location){
        //第一个座位是房主座位
        Seat ownerSeat = seats.get(0);
        if(ownerSeat != null){
            WxUser owner = ownerSeat.getWxUser();
            if(owner.getOpenid() == openid){
                pickRoleCard(location);
            }else{
                //do nothing
            }
        }else{
            //do nothing
        }
    }

    public void pickDesktopCard(String openid, int location){

    }

    public void pickSeat(String openid, int location){

    }

    public void sendMessage(String openid){

    }

    public void sendMessage(){

    }

    private void pickRoleCard(int location){
        //改变座位状态
        cardPicked[location] = !cardPicked[location];
        //刷新座位数
        if(cardPicked[location]){
            //如果新选中一张卡牌则添加一个座位
            seats.add(new Seat());
        }else{
            //否则减少最后一个座位
            seats.remove(seats.size() - 1);
        }
    }

    private Seat getSeatByWxUser(WxUser wxUser){
        for(Seat seat:seats){
            if(seat.getWxUser() == wxUser){
                return seat;
            }
        }
        return null;
    }

    private void pickReady(Seat seat){
        boolean ready = !seat.isReady();
        seat.setReady(ready);
        if(ready) {
            //检查是否能够触发游戏开始事件
            //如果玩家数量达到座位数量，且玩家都是ready状态则触发新游戏事件
            boolean allReady = true;
            for (Seat anySeat:seats) {
                if (!anySeat.isReady() || anySeat.getWxUser() == null) {
                    allReady = false;
                    break;
                }
            }
            if (allReady) {
                //新游戏创建后通知所有人
                newGame();
            } else {
                //人数未达到座位数游戏无法开始, 但准备状态改变了通知所有人
                sendMessage();
            }
        }else{
            //如果玩家取消准备那么不需要检查游戏是否开始
            sendMessage();
        }
    }

    private void newGame(){

    }
}
