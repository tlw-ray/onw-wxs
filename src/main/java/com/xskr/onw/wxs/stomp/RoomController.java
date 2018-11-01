package com.xskr.onw.wxs.stomp;

import com.xskr.onw.wxs.core.ClientAction;
import com.xskr.onw.wxs.core.Hall;
import com.xskr.onw.wxs.core.Room;
import com.xskr.onw.wxs.core.XskrMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;

import java.io.IOException;
import java.util.*;

@Controller
@MessageMapping("/onw/room")
@EnableScheduling
public class RoomController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public static final String KEY_OPEN_ID = "openID";
    public static final String KEY_ROOM_ID = "roomID";
    public static final String KEY_ROLE_CARD_ID = "roleCardID";
    public static final String KEY_DESKTOP_CARD_ID = "desktopCardID";
    public static final String KEY_SEAT_ID = "seatID";
    public static final String KEY_READY = "ready";

    @Autowired
    private Hall hall;
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

//    @SubscribeMapping("/topic/{roomID}")
//    public void subscribeRoomTopic(@DestinationVariable int roomID) {
//        System.out.println("subscribeRoomTopic: /topic/" + roomID);
//    }
//
//    @SubscribeMapping("/user/{openID}/message")
//    public void subscribeRoomMessage(@DestinationVariable String openID) {
//        System.out.println("subscribeRoomMessage: /user/" + openID + "/message/");
//    }

    @MessageMapping("/info")
    public void getRoomInformation(@Headers Map<String, LinkedMultiValueMap> headers, String payload){
        showHeader(headers, payload);
        String openID = getOpenID(headers);
        int roomID = getRoomID(headers);
        Room room = hall.getRoom(roomID);
        XskrMessage xskrMessage = new XskrMessage("Room information.", ClientAction.ROOM_CHANGED, room);
        simpMessagingTemplate.convertAndSendToUser(openID, "/message", xskrMessage);
    }

    /**
     * 玩家设定准备, 如果所有玩家均已准备则触发游戏开始
     */
    @MessageMapping("/ready")
    public void pickReady(@Headers Map<String, LinkedMultiValueMap> headers, String payload) {
        String openID = getOpenID(headers);
        int roomID = getRoomID(headers);
        Room room = hall.getRoom(roomID);
        room.switchReady(openID);
    }

//    @Scheduled(fixedRate = 5000)
//    public void sendToTopic(){
//        System.out.println("send");
//        simpMessagingTemplate.convertAndSend("/onw/room/topic/0", "test01");
//        simpMessagingTemplate.convertAndSend("/topic/0", "test02");
//    }

    /**
     * 设定房间角色和座位数
     */
    @MessageMapping("/roleCard")
    public void setRoleCards(@Headers Map<String, LinkedMultiValueMap> headers, String payload) throws IOException {
        String openID = getOpenID(headers);
        int roomID = getRoomID(headers);
        int roleCardID = getHeadIntegerValue(headers, KEY_ROLE_CARD_ID);
        System.out.println("roleCardID: " + roleCardID);
        Room room = hall.getRoom(roomID);
        room.pickRoleCard(openID, roleCardID);
    }

    /**
     * 玩家点击了桌上的一张牌
     */
    @MessageMapping("/desktopCard")
    public void pickDesktopCard(@Headers Map<String, LinkedMultiValueMap> headers, String payload) {
        System.out.println("desktopCard");
        String openID = getOpenID(headers);
        int roomID = getRoomID(headers);
        int desktopCardID = getHeadIntegerValue(headers, KEY_DESKTOP_CARD_ID);
        Room room = hall.getRoom(roomID);
        room.pickDesktopCard(openID, desktopCardID);
    }

    /**
     * 玩家点了某个座位
     */
    @MessageMapping("/seat")
    public void pickSeat(@Headers Map<String, LinkedMultiValueMap> headers, String payload) {
        System.out.println("seat");
        String openID = getOpenID(headers);
        int roomID = getRoomID(headers);
        int seatID = getHeadIntegerValue(headers, KEY_SEAT_ID);
        Room room = hall.getRoom(roomID);
        room.pickSeat(openID, seatID);
    }

    /**
     * 获得某个座位已有的关键信息
     */
    @MessageMapping("/keyMessages")
    public void getKeyMessages(@Headers Map<String, String> headers, String payload) {
        String openID = headers.get(KEY_OPEN_ID);
        Integer roomID = Integer.parseInt(headers.get(KEY_ROOM_ID));
        Room room = hall.getRoom(roomID);
        List<String> keyMessages = room.getKeyMessages(openID);
        //TODO send
    }

    private void showHeader(@Headers Map<String, LinkedMultiValueMap> headers, Object payload){
        System.out.println("Headers: ");
        for(Map.Entry entry:headers.entrySet())System.out.println("\t" + entry);
        System.out.println("Message: " + payload);
    }

    private void showHeader(@Headers Map<String, String> headers, String payload) {
        System.out.println("Headers: ");
        for (Map.Entry entry : headers.entrySet()) System.out.println("\t" + entry);
        System.out.println("Message: " + payload);
    }

    private String getOpenID(Map<String, LinkedMultiValueMap> headers){
        LinkedMultiValueMap nativeHeaders = headers.get("nativeHeaders");
        LinkedList openIDs = (LinkedList)nativeHeaders.get("openid");
        String openID = (String)openIDs.get(0);
        return openID;
    }

    private int getRoomID(Map<String, LinkedMultiValueMap> headers){
        LinkedMultiValueMap nativeHeaders = headers.get("nativeHeaders");
        LinkedList roomIDs = (LinkedList)nativeHeaders.get("roomID");
        int roomID = Integer.parseInt(roomIDs.get(0).toString());
        return roomID;
    }

    private int getHeadIntegerValue(Map<String, LinkedMultiValueMap> headers, String key){
        LinkedMultiValueMap nativeHeaders = headers.get("nativeHeaders");
        LinkedList roomIDs = (LinkedList)nativeHeaders.get(key);
        int roomID = Integer.parseInt(roomIDs.get(0).toString());
        return roomID;
    }

}
