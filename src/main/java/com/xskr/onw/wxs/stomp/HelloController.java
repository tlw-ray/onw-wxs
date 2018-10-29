package com.xskr.onw.wxs.stomp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

@Controller
@EnableScheduling
public class HelloController {

    @Autowired
    SimpMessageSendingOperations simpMessageSendingOperations;
    String openid = "omIzT5ALUJkupsn4TX_4NWUqqRwU";

    @MessageMapping("/messageMapping0")
    @SendTo("/topic/sendTo0")
    public Map<String, String> greeting(@Headers Map<String, String> headers, String message){
        System.out.println("Headers: ");
        for(Map.Entry entry:headers.entrySet())System.out.println("\t" + entry);
        System.out.println("Message: " + message);

        Map<String, String> result = new HashMap();
        result.put("say", "Hello");
        result.put("说", "你好");
        return result;
    }

    @SubscribeMapping("/topic/sendTo0")
    public String subscribeTopic(){
        String message = "订阅成功： /topic/sendTo0";
        System.out.println(message);
        return message;
    }

    //该功能能够运行，但为了不影响后面的功能暂时注释
//    @SubscribeMapping("/user/{openID}/message")
//    public String subscribeMessage(@DestinationVariable String openID){
//        String message = "订阅成功: /user/" + openID + "/message";
//        System.out.println(message);
//        return message;
//    }

    @MessageMapping("/messageMapping1")
    public void sendToUser(@Headers Map<String, LinkedMultiValueMap> headers, String message){
        System.out.println("Headers: ");
        for(Map.Entry entry:headers.entrySet())System.out.println("\t" + entry);
        System.out.println("Message: " + message);

        LinkedMultiValueMap nativeHeaders = headers.get("nativeHeaders");
        LinkedList openIDs = (LinkedList)nativeHeaders.get("openid");
        String openID = (String)openIDs.get(0);

        simpMessageSendingOperations.convertAndSendToUser(openID, "/message", "Queue Payload!!!");
    }

//下面代码能够成功的向小程序发送消息
//    @Scheduled(fixedRate = 2000)
//    public void sendMany(){
//        System.out.println("Send to user: ");
//        simpMessageSendingOperations.convertAndSendToUser("omIzT5ALUJkupsn4TX_4NWUqqRwU", "/message", "Queue Payload!!!");
//    }
}
