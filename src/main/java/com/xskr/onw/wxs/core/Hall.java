package com.xskr.onw.wxs.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Service
public class Hall {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    SimpMessagingTemplate simpMessagingTemplate;

    private static int RoomID_Generator = 0;
    private Map<Integer, Room> idRoomMap = Collections.synchronizedMap(new TreeMap());
    private Map<WxUser, Room> userRoomMap = Collections.synchronizedMap(new HashMap());

    public synchronized int create(){
        int roomID = RoomID_Generator++;
        Room room = new Room(roomID);
        room.setSimpMessagingTemplate(simpMessagingTemplate);
        idRoomMap.put(roomID, room);
        return roomID;
    }

    public synchronized boolean join(WxUser wxUser, int roomID){
        Room room = idRoomMap.get(roomID);
        if(room != null){
            //TODO 检查该玩家是否在其他房间没有退出
            //TODO 房间容量上限防止过多人加入
            room.join(wxUser);
            userRoomMap.put(wxUser, room);
            return true;
        }else{
            logger.error("加入房间失败，房间{}不存在.", roomID);
            return false;
        }
    }

    public synchronized boolean leave(String user, int roomID){
        for(Map.Entry entry:idRoomMap.entrySet())System.out.println(entry);
        Room room = idRoomMap.get(roomID);
        if(room != null) {
            room.leave(user);
            userRoomMap.remove(user);
            //检查房间内是否还有玩家
            boolean hasPlayer = false;
            for (Seat seat : room.getSeats()) {
                if (seat.getOpenid() != null) {
                    hasPlayer = true;
                    break;
                }
            }
            if (!hasPlayer) {
                //检查房间内是否还有观看者
                if (room.getObservers().size() == 0) {
                    //既没有玩家也没有观看者可以关闭房间
                    idRoomMap.remove(roomID);
                    logger.info("房间{}因玩家全部退出自动关闭。", roomID);
                } else {
                    //do nothing
                }
            } else {
                //do nothing
            }
            return true;
        }else{
            logger.error("离开房间失败: 房间{}不存在", roomID);
            return false;
        }
    }

    public Room getRoom(int roomID){
        return idRoomMap.get(roomID);
    }
}
