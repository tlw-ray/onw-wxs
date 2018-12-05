package com.xskr.onw.wxs.dealer;

import com.xskr.onw.wxs.card.*;
import com.xskr.onw.wxs.core.*;
import com.xskr.onw.wxs.core.message.SeatMessage;
import com.xskr.onw.wxs.rx.RxOnwRoom;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

//封装所有投票阶段的操作
public class VoteOnwDealer extends GamingOnwDealer{

    private boolean hunterVote = false;

    public VoteOnwDealer(RxOnwRoom room) {
        super(room);
    }

    public synchronized void initialize(){
        hunterVote = false;
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
        if (hunterVote) {
            //猎人技能投票
            hunterVote(user, id);
        } else {
            //普通vote
            Seat seat = room.getSeatByWxUser(user);
            if(seat.getVoteSeat() != null && id<room.getSeats().size() && id>-2){
                seat.setVoteSeat(id);
                if (canStatVote()) {
                    finishGame();
                }
            }
        }
    }

    //如果所有玩家都已经投票，那么可以统计投票数
    private boolean canStatVote(){
        for(Seat seat:room.getSeats()){
            if(seat.getVoteSeat() == null){
                return false;
            }
        }
        return true;
    }

    //如果统计的最高获票玩家中包含猎人，则由猎人独立投票，否则计算获胜阵营
    private VoteStat statVote(){
        //根据玩家的投票情况对每个玩家进行票数统计
        //并判断玩家队伍中是否存在狼
        //找到被投票最多的票数
        boolean hasWolfInPlayers = false;
        boolean hasHunterInPlayers = false;
        int maxVoteCount = 0;
        for(Seat seat:room.getSeats()){
            int voteSeat = seat.getVoteSeat();
            Seat votedPlayer = room.getSeats().get(voteSeat);
            votedPlayer.beVote();
            if(seat.getCard().getClass() == Wolf.class){
                hasWolfInPlayers = true;
            }else if(seat.getCard().getClass() == Doppelganger.class){
                Doppelganger doppelganger = (Doppelganger)seat.getCard();
                if(doppelganger.getAvatarCard().getClass() == Wolf.class){
                    hasWolfInPlayers = true;
                }
            }
            if(seat.getCard().getClass() == Hunter.class){
                hasHunterInPlayers = true;
            }
            if(seat.getVotedCount() > maxVoteCount){
                maxVoteCount = seat.getVotedCount();
            }

        }

        //找到该投票次数的玩家
        Set<Seat> maxVotedPlayerSet = new HashSet();
        //找到该投票次数的猎人玩家
        Set<Seat> maxVotedHunterSet = new HashSet();
        for(Seat seat:room.getSeats()){
            if(seat.getVotedCount() == maxVoteCount){
                maxVotedPlayerSet.add(seat);
                if(seat.isCard(Hunter.class)){
                    maxVotedHunterSet.add(seat);
                }
            }
        }
        return new VoteStat(hasWolfInPlayers, hasHunterInPlayers, maxVoteCount, maxVotedPlayerSet, maxVotedHunterSet);
    }

    //猎人投票
    protected void hunterVote(WxUser user, int id){
        //TODO 判定该事件是否能够触发
        Seat seat = room.getSeatByWxUser(user);
        if (seat.getCard().getClass() == Hunter.class ||
                (seat.getCard().getClass() == Doppelganger.class &&
                        ((Doppelganger)seat.getCard()).getAvatarCard().getClass() == Hunter.class)) {
            //猎人技能投票
            Seat votedSeat = room.getSeats().get(id);
            Set<Camp> victoryCampSet = new TreeSet();
            if (votedSeat.getCard().getClass() == Tanner.class) {
                //皮匠不存在阵营, 如果皮匠被投出则独自获胜
                victoryCampSet.add(Camp.TANNER);
            } else if (votedSeat.getCard().getClass() == Wolf.class) {
                victoryCampSet.add(Camp.VILLAGER);
            } else {
                victoryCampSet.add(Camp.WOLF);
            }
            gameFinish(victoryCampSet);
        } else {
            //do nothing
        }
    }

    private void gameFinish(Set<Camp> victoryCamp) {
        SeatMessage unreadyMessage = new SeatMessage("重新勾选‘准备’进入下一局...");
        for(Seat seat:room.getSeats()){
            Card card = seat.getCard();
            Camp camp = Camp.getCamp(card);
            //生成游戏结局
            seat.setOutcome(victoryCamp.contains(camp));
            //解除所有玩家的准备状态，本局游戏结束
            seat.setReady(false);
            seat.getInformation().add(unreadyMessage);
        }
        hunterVote = false;
        //游戏进入停止状态，可以重新准备触发下一轮开始
        room.setScene(Scene.PREPARE);
        room.sendMessage();
    }

    private void finishGame() {
        VoteStat voteStat = statVote();

        //分析获胜阵营: 狼人、村民、皮匠
        //按照如下顺序判定:
        //1. 如果玩家中没有狼人, 且每人得票数为1, 则共同获胜
        //2. 如果获得最大投票数的玩家中包含猎人身份，则由猎人获得当前投票结果后独立投票另一位玩家
        //3. 否则，如果只有皮匠获得最大票数，则皮匠阵营获胜
        //4. 否则，如果获得最大票数的角色中有狼，则村民获胜，皮匠和狼失败; 否则狼获胜，村民和皮匠失败

        // 统计并广播获胜阵营信息，游戏结束
        Set<Camp> victoryCamp = new TreeSet();
        Set<Camp> defeatCamp = new TreeSet();

        //只要皮匠被投出，皮匠即获胜
        //TODO 没有皮匠阵营, 即使有多个皮匠也只能独赢
        if(voteStat.voted(Tanner.class)){
            victoryCamp.add(Camp.TANNER);
        }else{
            defeatCamp.add(Camp.TANNER);
        }

        //根据玩家中有无狼来判断狼阵营和村阵营的输赢状况
        if (voteStat.hasWolfInPlayers()){
            // 如果所有玩家中有狼
            if(voteStat.getVotedHunterPlayer().size() > 0){
                // 如果有猎人被投中，则触发猎人技能
                // 告知猎人当前投票信息， 提示猎人由他独立投票
                StringBuilder report = new StringBuilder();
                for(Seat seat:room.getSeats()){
                    report.append(seat.getTitle());
                    if(seat.getVoteSeat() == Seat.VOTE_GIVE_UP){
                        //弃权
                        report.append("弃权");
                    }else{
                        Seat votedPlayer = room.getSeats().get(seat.getVoteSeat());
                        report.append("'投");
                        report.append(votedPlayer.getTitle());
                    }
                    report.append("'\n");
                }
                SeatMessage hunterSeatMessage = new SeatMessage(report.toString());
                for(Seat hunterSeat:voteStat.getVotedHunterPlayer()){
                    hunterSeat.getInformation().add(hunterSeatMessage);
                }
                hunterVote = true;
                room.sendMessage();
                return ;
            }else if(voteStat.voted(Wolf.class)){
                victoryCamp.add(Camp.VILLAGER);
                defeatCamp.add(Camp.WOLF);
            }else{
                victoryCamp.add(Camp.WOLF);
                defeatCamp.add(Camp.VILLAGER);
            }
        }else{
            //如果没有狼
            boolean allGiveUp = true;
            for(Seat seat:room.getSeats()){
                if(seat.getVoteSeat() != Seat.VOTE_GIVE_UP){
                    allGiveUp = false;
                    break;
                }
            }
            if (allGiveUp) {
                //共同获胜
                victoryCamp.add(Camp.VILLAGER);
                victoryCamp.add(Camp.WOLF);
            }else{
                //共同失败
                defeatCamp.add(Camp.VILLAGER);
                defeatCamp.add(Camp.WOLF);
            }
        }
        String outcomeInfo = String.format("[结局]: 村民(%s), 狼人(%s), 皮匠(%s)",
                getCampOutcome(Camp.VILLAGER, victoryCamp),
                getCampOutcome(Camp.WOLF, victoryCamp),
                getCampOutcome(Camp.TANNER, victoryCamp));

        SeatMessage outcomeMessage = new SeatMessage(outcomeInfo);
        room.setMessage(outcomeMessage);
        room.sendMessage();
        gameFinish(victoryCamp);
    }

    private String getCampOutcome(Camp camp, Set<Camp> victoryCamp){
        if(victoryCamp.contains(camp)){
            return "胜";
        }else{
            return "败";
        }
    }

}
