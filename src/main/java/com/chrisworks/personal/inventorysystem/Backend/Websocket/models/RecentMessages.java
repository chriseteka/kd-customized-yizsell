package com.chrisworks.personal.inventorysystem.Backend.Websocket.models;

import com.chrisworks.personal.inventorysystem.Backend.Websocket.models.enums.MESSAGE_FLOW;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecentMessages {
    private String body;
    private byte[] attachment;
    private Date sentDate;
    private Date sentTime;
    private MESSAGE_FLOW flow;
    private String fromEmail;
    private String toEmail;
}
