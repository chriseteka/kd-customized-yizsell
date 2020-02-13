package com.chrisworks.personal.inventorysystem.Backend.Websocket;

import lombok.Data;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Data
public class MessageObject {

    private String from;

    private String to;

    private String body;

    @Temporal(TemporalType.DATE)
    private Date dateSent = new Date();

    @Temporal(TemporalType.TIME)
    private Date timeSent = new Date();

    private MessageStatus status;

    public MessageObject(String from, String to, String body, MessageStatus status) {
        this.from = from;
        this.to = to;
        this.body = body;
        this.status = status;
    }
}
