package com.xskr.onw.wxs.intf;

import java.util.List;

public interface ISeat {
    IUser getUser();
    IUser getOldUser();//记录座位之前的玩家,用来实现断线重连
    boolean isReady();
    Integer getVoteSeat();
    int getVotedCount();
    boolean isEnable();
    Boolean getOutcome();
    List<IMessage> getInformation();
}
