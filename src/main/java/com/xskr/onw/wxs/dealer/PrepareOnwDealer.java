package com.xskr.onw.wxs.dealer;

import com.xskr.onw.wxs.core.Scene;
import com.xskr.onw.wxs.core.Seat;
import com.xskr.onw.wxs.core.WxUser;
import com.xskr.onw.wxs.rx.RxOnwRoom;
import lombok.extern.slf4j.Slf4j;

//封装所有准备阶段的操作
@Slf4j
public class PrepareOnwDealer extends OnwDealer{

    public PrepareOnwDealer(RxOnwRoom room) {
        super(room);
    }

    @Override
    public synchronized void join(WxUser user) {
        //房间的准备时段有人加入, 加入准备状态的房间,默认行为是找一个空座位坐下
        boolean seated = false;
        for(int i=0;i<room.getSeats().size();i++){
            Seat seat = room.getSeats().get(i);
            if(seat.getWxUser() == null){
                seat.setWxUser(user);
                seated = true;
                break;
            }else{
                //do nothing
            }
        }
        //如果没有空座位了就加入observer
        if(!seated){
            room.getObservers().add(user);
            room.sendMessage(user);
        }else{
            //立即通知所有人有新玩家加入
            room.sendMessage();
        }
    }

    @Override
    public synchronized void leave(WxUser user) {
        //在房间准备时段下退出
        if(room.getObservers().remove(user)){
            //observer退出导致房间改变但无需立即发送消息给所有人
        }else{
            //从座位上移除该玩家, 并设置该座位为非准备状态
            Seat playerSeat = room.getSeatByWxUser(user);
            if(playerSeat != null) {
                playerSeat.removePlayer();
                //房间变化了立即发送消息给所有人
                room.sendMessage();
            }else{
                String message = String.format("房间内不存在玩家%s.", user);
                log.error(message);
            }
        }
    }

    @Override
    public synchronized void pickDesktopCard(WxUser user, int id) {
        //准备时段点选桌上牌没有任何效果
    }

    @Override
    public synchronized void pickReady(WxUser user) {
        //准备时段如果已就坐玩家点击准备则改变其自身状态，并尝试触发AllReady事件
        Seat seat = room.getSeatByWxUser(user);
        if(seat != null) {
            //玩家准备状态改变
            boolean ready = !seat.isReady();
            seat.setReady(ready);
            if(ready) {
                //检查是否能够触发游戏开始事件
                //如果玩家数量达到座位数量，且玩家都是ready状态则触发新游戏事件
                boolean allReady = true;
                for (Seat anySeat:room.getSeats()) {
                    if (!anySeat.isReady() || anySeat.getWxUser() == null) {
                        allReady = false;
                        break;
                    }else{
                        //do nothing
                    }
                }
                if (allReady) {
                    //游戏开始
                    room.setScene(Scene.ACTIVATE);
                } else {
                    //人数未达到座位数游戏无法开始, 但准备状态改变了通知所有人
                    room.sendMessage();
                }
            }else{
                //如果玩家取消准备那么不需要检查游戏是否开始
                room.sendMessage();
            }
        }else{
            // do nothing
        }
    }

    @Override
    public synchronized void pickRoleCard(WxUser user, int id) {
        //准备时段房主点选房间配置卡会修改房间配置
        //第一个座位是房主座位
        Seat ownerSeat = room.getSeats().get(0);
        if (ownerSeat != null) {
            WxUser owner = ownerSeat.getWxUser();
            if (owner.getOpenid() == user.getOpenid()) {
                room.pickRoleCard(id);
            } else {
                //do nothing
            }
        } else {
            //do nothing
        }
    }

    @Override
    public synchronized void pickSeat(WxUser user, int id) {
        //准备时段点击座位
        Seat playerSeat = room.getSeatByWxUser(user);
        Seat targetSeat = room.getSeats().get(id);
        if(targetSeat.getWxUser() == null){
            //点击到空座位
            if(playerSeat != null){
                //如果是玩家点击座位
                if(playerSeat.isReady()){
                    //如果该玩家已经准备了不能换座位
                }else if(playerSeat == targetSeat){
                    //玩家点击自己的座位离开座位
                    playerSeat.removePlayer();
                    room.getObservers().add(user);
                    room.sendMessage();
                }else{
                    //玩家点击换到空座位
                    playerSeat.setWxUser(null);
                    targetSeat.setWxUser(user);
                    room.sendMessage();
                }
            }else if(room.getObservers().contains(user)){
                //如果是观察者点击空座位则转变为玩家
                targetSeat.setWxUser(user);
                room.getObservers().remove(user);
                room.sendMessage();
            }
        }else{
            //玩家或观看者点击已经有人的座位不会产生任何效果
        }
    }

    @Override
    public void initialize() {
        for(Seat seat:room.getSeats()){
            seat.setReady(false);
            seat.reset();
        }
    }
}
