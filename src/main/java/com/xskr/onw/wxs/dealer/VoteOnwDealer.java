package com.xskr.onw.wxs.dealer;

import com.xskr.onw.wxs.core.WxUser;
import com.xskr.onw.wxs.rx.RxOnwRoom;

//封装所有投票阶段的操作
public class VoteOnwDealer extends GamingOnwDealer{

    public VoteOnwDealer(RxOnwRoom room) {
        super(room);
    }

    public synchronized void initialize(){

    }

    @Override
    public synchronized void leave(WxUser user) {
        //投票过程中有人离开座位, 告知所有玩家
        super.leave(user);
        //TODO 当所有人都投票后系统会代理该玩家投弃权票
    }

    @Override
    public void auto() {
        //TODO 自动投票
    }

    @Override
    public synchronized void pickDesktopCard(WxUser user, int id) {
        //投票过程中点击桌面牌不会产生任何效果
    }

    @Override
    public synchronized void pickReady(WxUser user) {
        //投票过程中点击准备不会产生任何效果
    }

    @Override
    public synchronized void pickRoleCard(WxUser user, int id) {
        //投票过程中点击规则卡暂时不会产生任何效果, 后面可能可以加入辅助分析功能
    }

    @Override
    public synchronized void pickSeat(WxUser user, int id) {
        //投票过程中点击座位表示投该玩家的票

    }
}
