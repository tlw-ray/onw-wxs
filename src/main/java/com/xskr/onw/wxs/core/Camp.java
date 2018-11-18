package com.xskr.onw.wxs.core;

import com.xskr.onw.wxs.core.card.Card;
import com.xskr.onw.wxs.core.card.Minion;
import com.xskr.onw.wxs.core.card.Tanner;
import com.xskr.onw.wxs.core.card.Wolf;

import java.util.HashSet;
import java.util.Set;

public enum Camp {

    WOLF, VILLAGER, TANNER;

    public static boolean isWolfCamp(Card card){
        return card.getClass() == Wolf.class
                || card.getClass() == Minion.class;
    }

    public static Camp getCamp(Card card){
        if(isTannerCamp(card)){
            return VILLAGER;
        }else if(isWolfCamp(card)){
            return WOLF;
        }else{
            return VILLAGER;
        }
    }

//    public static boolean isVillagerCamp(Card card){
//        return !isWolfCamp(card) && !isTannerCamp(card);
//    }

    public static boolean isTannerCamp(Card card){
        return card.getClass() == Tanner.class;
    }

//    public static Set<Card> getCards(Camp camp){
//        Set<Card> result = new HashSet();
//        for(Card card : CardFactory.CARDS){
//            switch(camp){
//                case WOLF: if(isWolfCamp(card)) result.add(card); break;
//                case TANNER: if(isTannerCamp(card)) result.add(card); break;
//                case VILLAGER: if(isVillagerCamp(card)) result.add(card); break;
//            }
//        }
//        return result;
//    }
}
