package com.chrisworks.personal.inventorysystem.Backend.Websocket;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebsocketConfiguration implements WebSocketMessageBrokerConfigurer {

    private AuthChannelInterceptorAdapter authChannelInterceptorAdapter;

    @Autowired
    public WebsocketConfiguration(AuthChannelInterceptorAdapter authChannelInterceptorAdapter) {
        this.authChannelInterceptorAdapter = authChannelInterceptorAdapter;

    }

    //Stomp client = ws://localhost:7000/api/inventory/chat
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/chat")
                .setHandshakeHandler(new CustomHandshakeHandler())
                .setAllowedOrigins("*")
                .withSockJS();
    }

    //Clients can subscribe to:
    //1. /topic/general => General group for everyone
    //2. /user/queue/reply => Direct body to single user
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config){
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void configureClientInboundChannel(final ChannelRegistration registration) {
        registration.interceptors(authChannelInterceptorAdapter);
    }
}