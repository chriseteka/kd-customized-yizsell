package com.chrisworks.personal.inventorysystem.Backend.Websocket.models;

import com.chrisworks.personal.inventorysystem.Backend.Websocket.models.enums.MessageStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

/**
 * @author Chris_Eteka
 * @since 4/21/2020
 * @email chriseteka@gmail.com
 */
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @OneToOne
    private UserMiniProfile from;
    @OneToOne
    private UserMiniProfile to;
    @Lob
    private String body;
    private byte[] attachment;
    @Temporal(TemporalType.DATE)
    private Date sentDate = new Date();
    @Temporal(TemporalType.TIME)
    private Date sentTime = new Date();

    private MessageStatus status = MessageStatus.SENT;

    public Message(UserMiniProfile from, UserMiniProfile to, String body, byte[] attachment) {
        this.from = from;
        this.to = to;
        this.body = body;
        this.attachment = attachment;
    }
}
