package com.xskr.onw.wxs.ws;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
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

    @MessageMapping("/onw/hello")
    @SendTo("/topic/greetings")
    public Map<String, String> greeting(@Headers Map<String, String> headers, String message){
        System.out.println("Headers: ");
        for(Map.Entry entry:headers.entrySet())System.out.println("\t" + entry);
        System.out.println("Message: " + message);

        Map<String, String> result = new HashMap();
        result.put("say", "Hello");
        result.put("说", "你好");
        return result;
    }

//    @Scheduled(fixedRate = 1000)
//    public void sendToUser() {
//        System.out.println("Send to user: ...");
//        String openid = "60c570f7a4343d9645c6ba44be2baad2";
//        simpMessageSendingOperations.convertAndSendToUser(openid, "/message", "你好" + openid);
//    }

    @MessageMapping("/onw/bye")
    public void sendToUser(@Headers Map<String, LinkedMultiValueMap> headers, String message){
        System.out.println("Headers: ");
        for(Map.Entry entry:headers.entrySet())System.out.println("\t" + entry);
        System.out.println("Message: " + message);

        LinkedMultiValueMap nativeHeaders = headers.get("nativeHeaders");
        LinkedList openIDs = (LinkedList)nativeHeaders.get("openid");
        String openID = (String)openIDs.get(0);

        simpMessageSendingOperations.convertAndSendToUser(openID, "/queue", "Queue Payload!!!");
    }

    @Scheduled(fixedRate = 2000)
    public void sendMany(){
        System.out.println("send many: ");
        simpMessageSendingOperations.convertAndSendToUser("omIzT5ALUJkupsn4TX_4NWUqqRwU", "/message", "Queue Payload!!!");
    }
}
