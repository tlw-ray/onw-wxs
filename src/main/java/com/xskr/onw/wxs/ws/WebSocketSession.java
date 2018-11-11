package com.xskr.onw.wxs.ws;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xskr.onw.wxs.core.Hall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint(value = "/websocket")
@Component
public class WebSocketSession {

    private static Logger logger = LoggerFactory.getLogger(WebSocketSession.class);

    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int onlineCount = 0;
    //concurrent包的线程安全Set，用来存放每个客户端对应的WebSocketServer对象。
    private static CopyOnWriteArraySet<WebSocketSession> webSocketSet = new CopyOnWriteArraySet();

    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;

    @Autowired
    private Hall hall;

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        webSocketSet.add(this);     //加入set中
        addOnlineCount();           //在线数加1
        logger.info("有新连接加入！当前在线人数为" + getOnlineCount());
        sendMessage("连接成功");
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        webSocketSet.remove(this);  //从set中删除
        subOnlineCount();           //在线数减1
        logger.info("有一连接关闭！当前在线人数为" + getOnlineCount());
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        logger.info("来自客户端的消息:" + message);
        //群发消息
        for (WebSocketSession item : webSocketSet) {
            item.sendMessage(message);
        }
    }

    /**
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        logger.error("发生错误");
        error.printStackTrace();
    }


    public void sendMessage(String message){
        System.out.println("sendMessage: " + message);
        this.session.getAsyncRemote().sendText(message);
    }

    public static synchronized int getOnlineCount(){
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        WebSocketSession.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        WebSocketSession.onlineCount--;
    }
}
