package com.xskr.onw.wxs;

import com.xskr.onw.wxs.core.CardFactory;
import com.xskr.onw.wxs.core.role.Card;
import org.junit.Test;

public class Test01 {
    @Test
    public void test01(){
        for(Card card: CardFactory.CARDS){
            System.out.println(card);
        }
    }
}
