package com.chrisworks.personal.inventorysystem.Backend.Websocket.models;

import com.chrisworks.personal.inventorysystem.Backend.Websocket.models.enums.MessageStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageDto {

    private String fromEmail;
    private String toEmail;
    private String body;
    private byte[] attachment = null;
    private Date sentDate;
    private Date sentTime;
    private MessageStatus status;
}
