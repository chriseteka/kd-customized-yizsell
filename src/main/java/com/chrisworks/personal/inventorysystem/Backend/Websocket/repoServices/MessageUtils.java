package com.chrisworks.personal.inventorysystem.Backend.Websocket.repoServices;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Warehouse;
import com.chrisworks.personal.inventorysystem.Backend.Websocket.models.Message;
import com.chrisworks.personal.inventorysystem.Backend.Websocket.models.MessageDto;

import java.util.List;

public interface MessageUtils {

    Message persistMessage(MessageDto messageDto);

    MessageDto fromMessageToDTO(Message message);

    boolean verifyEmail(List<String> emails);

    List<MessageDto> notifyWarehouseAttendants(Warehouse warehouse, String notice);

    List<MessageDto> notifyUser(String notice, String... emails);

    String getBusinessManagerMail();
}
