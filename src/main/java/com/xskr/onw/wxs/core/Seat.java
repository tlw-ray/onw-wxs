package com.xskr.onw.wxs.core;

import com.xskr.onw.wxs.card.Card;
import com.xskr.onw.wxs.card.Doppelganger;
import com.xskr.onw.wxs.core.message.SeatMessage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 座位
 * -1: 是默认的observer的座位
 * 0到(房间可用卡牌数量-3): 是场上玩家的座位
 * 其余座位也是无效座位
 * 因为桌游不同于以往游戏，可能要有人会临时退出或加入，实际上桌游的进展是以玩家座位为不变的基础进行的
 */
public class Seat{

	public static final int VOTE_GIVE_UP = -1;

	int id;
	//座位上的玩家如果为null说明该座位没有人坐
	private String openid;
	private WxUser wxUser;
	//该座位之前玩家，用于断线重连，这名玩家可能断线了也可能有事情离开了房间
	private String oldUserName;
	//该座位的初始卡牌
	private Card initializeCard;
	//初始规则, 目前仅有孤狼一条规则, 需要由服务端告知客户端
    private Set<Role> initializeRole = new HashSet();
	//该座位的当前卡牌(卡牌可能会经历某些交换操作)
	private Card card;
	//该座位是否声明已经准备好可以开始了
	private boolean ready;
	//该座位的玩家投票到某个座位的玩家
	private Integer voteSeat;
	//该座位玩家被投票的次数
	private int votedCount;
	//该座位是否可用，例如: 选中了5张卡牌，去掉桌面的3张牌，前2个座位就是可用的
	private boolean enable = true;
    //本局是否胜利
    private Boolean outcome = null;
	//该玩家的关键信息，供断线重连时提供
	private List<SeatMessage> information = new ArrayList();

	public Seat(int id){
		this.id = id;
	}

	//一局游戏结束重置玩家状态
	public void reset(){
	    initializeCard = null;
	    card = null;
	    voteSeat = null;
	    votedCount = 0;
	    outcome = null;
	    information.clear();
    }

    public String getTitle(){
		return String.format("%s. [%s], ", id, wxUser.getNickName());
	}

	public void removePlayer(){
		this.setWxUser(null);
		this.setReady(false);
	}

	public boolean isCard(Class<? extends Card> cardClass){
        return getCard().getClass() == cardClass || (
                getCard().getClass() == Doppelganger.class &&
                        ((Doppelganger) getCard()).getAvatarCard().getClass() == cardClass);
    }

    @Deprecated
	public String getOpenid() {
		return openid;
	}

	public Card getCard() {
		return card;
	}

	public void setCard(Card card) {
		this.card = card;
	}

	public boolean isReady() {
		return ready;
	}

	public void setReady(boolean ready) {
		this.ready = ready;
	}

	public Integer getVoteSeat() {
		return voteSeat;
	}

	public void setVoteSeat(Integer voteSeat) {
		this.voteSeat = voteSeat;
	}

	public int getVotedCount() {
		return votedCount;
	}

	public void setVotedCount(int votedCount) {
		this.votedCount = votedCount;
	}

	public void beVote(){
		votedCount++;
	}

	public Card getInitializeCard() {
		return initializeCard;
	}

	public void setInitializeCard(Card initializeCard) {
		this.initializeCard = initializeCard;
	}

	public void setOpenid(String openid) {
		this.openid = openid;
		//设定旧的玩家
		if(openid != null){
			this.oldUserName = openid;
		}
	}

	public List<SeatMessage> getInformation() {
		return information;
	}

	public void setInformation(List<SeatMessage> information) {
		this.information = information;
	}

	public String getOldUserName() {
		return oldUserName;
	}

	public void setOldUserName(String oldUserName) {
		this.oldUserName = oldUserName;
	}

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

    public Set<Role> getInitializeRole() {
        return initializeRole;
    }

    public Boolean getOutcome() {
        return outcome;
    }

    public void setOutcome(Boolean outcome) {
        this.outcome = outcome;
    }

	public WxUser getWxUser() {
		return wxUser;
	}

	public void setWxUser(WxUser wxUser) {
		this.wxUser = wxUser;
		this.ready = false;
	}

	@Override
    public String toString() {
        return "Seat{" +
                "openid='" + openid + '\'' +
                ", card=" + card +
                ", voteSeat=" + voteSeat +
                '}';
    }
}
