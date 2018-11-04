package com.xskr.onw.wxs.core.card;

public class Villager extends Card {
    public Villager(int id){
        this.id = id;
    }

    @Override
    public String getDisplayName() {
        return "村民";
    }
}
