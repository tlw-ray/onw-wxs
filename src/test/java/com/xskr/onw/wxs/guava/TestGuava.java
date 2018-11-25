package com.xskr.onw.wxs.guava;

import com.google.common.eventbus.EventBus;
import org.junit.Test;

public class TestGuava {
    @Test
    public void testEventBus(){
        EventBus eventBus = new EventBus();
        GeventListener listener = new GeventListener();
        eventBus.register(listener);

        eventBus.post(new HelloEvent("hello"));
        eventBus.post(new WorldEvent("world", 23333));
    }
}
