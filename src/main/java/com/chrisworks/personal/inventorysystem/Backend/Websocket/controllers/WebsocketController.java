package com.chrisworks.personal.inventorysystem.Backend.Websocket.controllers;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Warehouse;
import com.chrisworks.personal.inventorysystem.Backend.Websocket.models.Message;
import com.chrisworks.personal.inventorysystem.Backend.Websocket.models.MessageDto;
import com.chrisworks.personal.inventorysystem.Backend.Websocket.repoServices.MessageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;


@RequiredArgsConstructor
@RestController
public class WebsocketController {

    private final SimpMessageSendingOperations simpMessagingTemplate;
    private final MessageUtils messageUtils;

    //Messages will be sent to: /app/chat/{email}
    @MessageMapping("/chat/{to}")
    public void sendMessage(@DestinationVariable String to, MessageDto message) {

        if (!to.equalsIgnoreCase(message.getToEmail())) return;

        boolean isExists = messageUtils.verifyEmail(Arrays.asList(to, message.getFromEmail()));

        if (isExists) {

            Message persistedMessage = messageUtils.persistMessage(message);
            if (persistedMessage != null)
                simpMessagingTemplate.convertAndSend("/topic/messages/" + to,
                        messageUtils.fromMessageToDTO(persistedMessage));
        }
    }

    //Messages will be sent to: /app/broadcast/{email1, email2, ..., emailLast}
    @MessageMapping("/broadcast/{to}")
    public void sendMessageBroadcast(@DestinationVariable("to") String[] emailList, MessageDto message) {

        if (!message.getToEmail().isEmpty()) return;

        System.out.println("handling send message: " + message + " to: " + Arrays.toString(emailList));

        List<String> emails = Arrays.asList(emailList);
        emails.add(message.getFromEmail());
        boolean isExists = messageUtils.verifyEmail(emails);
        emails.remove(message.getFromEmail());

        if (isExists) {
            for (String toEmail: emails) {
                message.setToEmail(toEmail);
                Message persistedMessage = messageUtils.persistMessage(message);
                if (persistedMessage != null)
                    simpMessagingTemplate.convertAndSend("/topic/messages/" + toEmail,
                            messageUtils.fromMessageToDTO(persistedMessage));
            }
        }
    }

    public void sendNoticeToWarehouseAttendants(Warehouse warehouse, String notice){

        sendMultipleMessages(messageUtils.notifyWarehouseAttendants(warehouse, notice));
    }

    public String businessOwnerMail(){
        return messageUtils.getBusinessManagerMail();
    }

    public void sendNoticeToUser(String notice, String... userMails){

        sendMultipleMessages(messageUtils.notifyUser(notice, userMails));
    }

    private void sendMultipleMessages(List<MessageDto> messageDtoList) {

        if (messageDtoList == null || messageDtoList.isEmpty()) return;

        messageDtoList.forEach(messageDto -> sendMessage(messageDto.getToEmail(), messageDto));
    }
}
