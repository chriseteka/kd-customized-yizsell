package com.chrisworks.personal.inventorysystem.Backend.Websocket.controllers;

import com.chrisworks.personal.inventorysystem.Backend.Websocket.models.Message;
import com.chrisworks.personal.inventorysystem.Backend.Websocket.models.MessageDto;
import com.chrisworks.personal.inventorysystem.Backend.Websocket.repoServices.MessageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;


@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class WebsocketController {

    private final SimpMessageSendingOperations simpMessagingTemplate;
    private final MessageUtils messageUtils;

    @MessageMapping("/chat/{to}")
    public void sendMessage(@DestinationVariable String to, MessageDto message) {

        if (!to.equalsIgnoreCase(message.getToEmail())) return;

        System.out.println("handling send message: " + message + " to: " + to);

        boolean isExists = messageUtils.verifyEmail(to, message.getFromEmail());

        if (isExists) {

            Message persistedMessage = messageUtils.persistMessage(message);
            if (persistedMessage != null)
                simpMessagingTemplate.convertAndSend("/topic/messages/" + to,
                        messageUtils.fromMessageToDTO(persistedMessage));
        }
    }
}
