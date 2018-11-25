package com.xskr.onw.wxs.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

//执行类，测试入口
@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args){
        ApplicationContext context = SpringApplication.run(DemoApplication.class, args);
        String[] names = context.getBeanDefinitionNames();
        for(int i=0; i<names.length; i++) {
            System.out.println(names[i]);
        }
        System.out.println("++++++++++");
//        context.publishEvent(new HelloEvent("helloEvent"));
        context.publishEvent(new CustomerEvent("customer", true));
        context.publishEvent(new CustomerEvent("miaomiao", false));
    }
}
