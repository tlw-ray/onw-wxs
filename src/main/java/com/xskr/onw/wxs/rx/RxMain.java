package com.xskr.onw.wxs.rx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class RxMain {
    public static ApplicationContext context;
    public static void main(String[] args){
        context = SpringApplication.run(RxMain.class, args);
    }
}
