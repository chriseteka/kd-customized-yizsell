package com.chrisworks.personal.inventorysystem.Backend.Websocket.models;

import com.chrisworks.personal.inventorysystem.Backend.Websocket.models.enums.MESSAGE_FLOW;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecentMessages {
    private String body;
    private byte[] attachment;
    @Temporal(TemporalType.DATE)
    private Date dateSent = new Date();
    @Temporal(TemporalType.TIME)
    private Date timeSent = new Date();
    private MESSAGE_FLOW flow;
}
