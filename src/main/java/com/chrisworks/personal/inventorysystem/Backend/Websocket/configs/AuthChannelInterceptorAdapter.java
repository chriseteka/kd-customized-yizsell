package com.chrisworks.personal.inventorysystem.Backend.Websocket.configs;

import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import com.chrisworks.personal.inventorysystem.Backend.Websocket.repoServices.Impl.WebsocketAuthenticatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class AuthChannelInterceptorAdapter implements ChannelInterceptor {

    private static String USERNAME_HEADER;
    private final WebsocketAuthenticatorService webSocketAuthenticatorService;
    @Autowired
    public AuthChannelInterceptorAdapter(final WebsocketAuthenticatorService webSocketAuthenticatorService) {
        this.webSocketAuthenticatorService = webSocketAuthenticatorService;
    }

    @Override
    public Message<?> preSend(final Message<?> message, final MessageChannel channel) throws AuthenticationException {
        final StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        assert accessor != null;
        if (accessor.getCommand() == StompCommand.DISCONNECT){
            System.out.println("You may have been forcefully disconnected");
            return null;
        }
        USERNAME_HEADER = AuthenticatedUserDetails.getUserFullName();
        final UsernamePasswordAuthenticationToken user = webSocketAuthenticatorService
                .getAuthenticatedOrFail(USERNAME_HEADER);
        if (StompCommand.CONNECT == accessor.getCommand() && accessor.getUser() == null)
            accessor.setUser(user);
        return message;
    }
}
