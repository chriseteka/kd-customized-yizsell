package com.chrisworks.personal.inventorysystem.Backend.Websocket.repoServices;

import com.chrisworks.personal.inventorysystem.Backend.Websocket.models.Message;
import com.chrisworks.personal.inventorysystem.Backend.Websocket.models.MessageDto;

public interface MessageUtils {

    Message persistMessage(MessageDto messageDto);

    MessageDto fromMessageToDTO(Message message);

    boolean verifyEmail(String... args);
}
