package com.xskr.onw.wxs.core;

public enum Card {
	DOPPELGANGER,
	WEREWOLF_1, WEREWOLF_2, MINION,
	MASON_1, MASON_2,
	SEER, ROBBER, TROUBLEMAKER,
	DRUNK, INSOMNIAC,
	HUNTER, TANNER,
	VILLAGER_1, VILLAGER_2, VILLAGER_3;


//	ResourceBundle resource = ResourceBundle.getBundle(getClass().getOpenid());
//  //去掉了ResourceBundle，发现springboot在Docker下这样做会导致ClassDefNotFoundException异常
//	public String getDisplayName(){
//		return resource.getString(this.name());
//	}
	public static int index(Card card){
		for(int i=0;i<values().length;i++){
			if(values()[i] == card){
				return i;
			}
		}
		return -1;
	}
}
