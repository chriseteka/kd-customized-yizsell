package com.chrisworks.personal.inventorysystem.Backend.Websocket;

import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

public class CustomHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {

        System.out.println("Request: " + request);
        return new ChatPrincipal(AuthenticatedUserDetails.getUserFullName());
    }

    @Data
    @AllArgsConstructor
    class ChatPrincipal implements Principal{

        String name;

        @Override
        public String getName() {
            return name;
        }
    }
}
