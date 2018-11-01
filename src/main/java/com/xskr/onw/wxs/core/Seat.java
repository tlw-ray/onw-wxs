package com.xskr.onw.wxs.core;

import java.util.ArrayList;
import java.util.List;

/**
 * 座位
 * -1: 是默认的observer的座位
 * 0到(房间可用卡牌数量-3): 是场上玩家的座位
 * 其余座位也是无效座位
 * 因为桌游不同于以往游戏，可能要有人会临时退出或加入，实际上桌游的进展是以玩家座位为不变的基础进行的
 */
public class Seat{
	//座位上的玩家如果为null说明该座位没有人坐
	private String openid;
	//该座位之前玩家，用于断线重连，这名玩家可能断线了也可能有事情离开了房间
	private String oldUserName;
	//该座位的初始卡牌
	private Card initializeCard;
	//该座位的当前卡牌(卡牌可能会经历某些交换操作)
	private Card card;
	//该座位是否声明已经准备好可以开始了
	private boolean ready;
	//该座位的玩家投票到某个座位的玩家
	private Integer voteSeat;
	//该座位玩家被投票的次数
	private int votedCount;
	//该座位是否可用
	private boolean enable = true;
	//该玩家的关键信息，供断线重连时提供
	private List<XskrMessage> keyMessages = new ArrayList();

	//一局游戏结束重置玩家状态
	public void reset(){
	    initializeCard = null;
	    card = null;
	    ready = false;
	    voteSeat = null;
	    keyMessages.clear();
    }
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

	void setReady(boolean ready) {
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

	public List<XskrMessage> getKeyMessages() {
		return keyMessages;
	}

	public void setKeyMessages(List<XskrMessage> keyMessages) {
		this.keyMessages = keyMessages;
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

    @Override
    public String toString() {
        return "Seat{" +
                "openid='" + openid + '\'' +
                ", card=" + card +
                ", voteSeat=" + voteSeat +
                '}';
    }
}
