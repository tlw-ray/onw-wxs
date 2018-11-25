package com.xskr.onw.wxs.core;

import com.xskr.onw.wxs.card.*;

public class CardFactory {

    public final Doppelganger DOPPELGANGER = new Doppelganger();
    public final Wolf WOLF_0 = new Wolf();
    public final Wolf WOLF_1 = new Wolf();
    public final Minion MINION = new Minion();
    public final Mason MASON_0 = new Mason();
    public final Mason MASON_1 = new Mason();
    public final Seer SEER = new Seer();
    public final Robber ROBBER = new Robber();
    public final Troublemaker TROUBLEMAKER = new Troublemaker();
    public final Drunk DRUNK = new Drunk();
    public final Insomniac INSOMNIAC = new Insomniac();
    public final Hunter HUNTER = new Hunter();
    public final Villager VILLAGER_0 = new Villager();
    public final Villager VILLAGER_1 = new Villager();
    public final Villager VILLAGER_2 = new Villager();
    public final Tanner TANNER = new Tanner();

    //注意这里顺序要和界面上一样
    public final Card[] CARDS = new Card[]{
        DOPPELGANGER,
        WOLF_0, WOLF_1, MINION,
        SEER, ROBBER, TROUBLEMAKER, INSOMNIAC,
        HUNTER, DRUNK,
        MASON_0, MASON_1,
        VILLAGER_0, VILLAGER_1, VILLAGER_2,
        TANNER
    };

    public int index(Card card){
        for(int i=0;i<CARDS.length;i++){
            if(CARDS[i] == card){
                return i;
            }
        }
        return -1;
    }

}
