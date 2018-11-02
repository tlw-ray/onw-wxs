package com.xskr.onw.wxs.core;

import com.xskr.onw.wxs.core.role.*;

public class CardFactory {

    public static final Doppelganger DOPPELGANGER = new Doppelganger();
    public static final Wolf WOLF_0 = new Wolf(0);
    public static final Wolf WOLF_1 = new Wolf(1);
    public static final Minion MINION = new Minion();
    public static final Mason MASON_0 = new Mason(0);
    public static final Mason MASON_1 = new Mason(1);
    public static final Seer SEER = new Seer();
    public static final Robber ROBBER = new Robber();
    public static final Troublemaker TROUBLEMAKER = new Troublemaker();
    public static final Drunk DRUNK = new Drunk();
    public static final Insomniac INSOMNIAC = new Insomniac();
    public static final Hunter HUNTER = new Hunter();
    public static final Villager VILLAGER_0 = new Villager(0);
    public static final Villager VILLAGER_1 = new Villager(1);
    public static final Villager VILLAGER_2 = new Villager(2);
    public static final Tanner TANNER = new Tanner();

    public static final Card[] CARDS = new Card[]{
        DOPPELGANGER,
        WOLF_0, WOLF_1, MINION,
        MASON_0, MASON_1,
        SEER, ROBBER, TROUBLEMAKER, DRUNK, INSOMNIAC,
        HUNTER, VILLAGER_0, VILLAGER_1, VILLAGER_2,
        TANNER
    };

    public static int index(Card card){
        for(int i=0;i<CARDS.length;i++){
            if(CARDS[i] == card){
                return i;
            }
        }
        return -1;
    }

}
