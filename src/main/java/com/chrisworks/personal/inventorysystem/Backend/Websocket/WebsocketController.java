package com.chrisworks.personal.inventorysystem.Backend.Websocket;

import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Component;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Component
public class WebsocketController {

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    //e.g: Direct body to a user goes to: /app/direct/message
    @MessageMapping("/direct")
    @SendToUser("/queue/reply")
    public OutputMessage sendMessageFromUserToUser(@Payload MessageObject message) throws Exception {
        messagingTemplate
                .convertAndSendToUser(message.getTo(), "/queue/reply", message);

        return new OutputMessage(message.getFrom(), message.getBody(), message.getTimeSent());
    }

    //e.g: Direct body to a user goes to: /app/group/message
    @MessageMapping("/group")
    @SendTo("/topic/general")
    public OutputMessage send(@Payload String message) throws Exception {

        System.out.println("Auth User: " + AuthenticatedUserDetails.getUserFullName());
        System.out.println("message: " + message);
        messagingTemplate.convertAndSend("/topic/general", message);

        return null;
//        return new OutputMessage(message.getFrom(), message.getBody(), message.getTimeSent());
    }

    @MessageExceptionHandler
    @SendToUser("/queue")
    public String handleException(Throwable exception) {
        return exception.getMessage();
    }

    @Data
    @AllArgsConstructor
    private class OutputMessage{

        String from;

        String text;

        @Temporal(TemporalType.TIME)
        Date time;
    }
}
