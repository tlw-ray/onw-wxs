package com.xskr.onw.wxs.dealer;

import com.xskr.onw.wxs.core.Seat;
import com.xskr.onw.wxs.core.WxUser;
import com.xskr.onw.wxs.rx.RxOnwRoom;

//游戏进行中的操作
public abstract class GamingOnwDealer extends OnwDealer{
    public GamingOnwDealer(RxOnwRoom room) {
        super(room);
    }

    public synchronized void join(WxUser user) {
        //游戏在进行中有人加入
        Seat oldUserSeat = null;
        for(Seat seat:room.getSeats()){
            WxUser seatUser = seat.getWxUser();
            if(seatUser == null && seat.getOldUserName().equals(user.getOpenid())){
                oldUserSeat = seat;
            }
        }

        if(oldUserSeat != null){
            //玩家之前从座位上离开了，现在回到了座位
            oldUserSeat.setWxUser(user);
            room.sendMessage();
        }else{
            //加入observer, 但无需通知玩家
            room.getObservers().add(user);
            room.sendMessage(user);
        }
    }
    public synchronized void leave(WxUser user) {
        //行动状态下离开房间, 设定座位上的玩家为空, TODO 并为玩家执行随机操作
        Seat seat = room.getSeatByWxUser(user);
        seat.removePlayer();
        room.sendMessage();
    }

    //因玩家离开游戏,所有在座玩家操作完后提供自动操作
    public abstract void auto();
}
