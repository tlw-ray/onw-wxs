package com.xskr.onw.wxs;

import com.xskr.onw.wxs.core.Card;
import org.junit.Test;

public class Test01 {
    @Test
    public void test01(){
        for(Card card:Card.values()){
            System.out.println(card);
        }
    }
}
