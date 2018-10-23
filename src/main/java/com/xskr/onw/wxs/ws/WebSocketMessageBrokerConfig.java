package com.xskr.onw.wxs.ws;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketMessageBrokerConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 这句话表示在topic和user这两个域上可以向客户端发消息。
        config.enableSimpleBroker("/topic", "/user");
        // 这句话表示客户单向服务器端发送时的主题上面需要加"/app"作为前缀。
        config.setApplicationDestinationPrefixes("/app");
        // 这句话表示给指定用户发送一对一的主题前缀是"/user"。
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/onw/endpoint")
                .addInterceptors(new HttpHandshakeInterceptor())
                .setAllowedOrigins("*");//.withSockJS();
    }

}
