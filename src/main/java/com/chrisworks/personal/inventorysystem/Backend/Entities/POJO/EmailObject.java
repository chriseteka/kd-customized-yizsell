package com.chrisworks.personal.inventorysystem.Backend.Entities.POJO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Chris_Eteka
 * @since 11/25/2019
 * @email chriseteka@gmail.com
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailObject {

    @Email(message = "Invalid body sender, please check sender's email address")
    private String messageSender;

    @Email(message = "Invalid body receiver, please check receiver's email address")
    private String messageReceiver;

    @NotEmpty(message = "body title cannot be empty")
    private String messageTitle;

    @NotEmpty(message = "body title cannot be empty")
    private String messageBody;

    private List<EmailAttachments> attachments = new ArrayList<>(Collections.emptyList());

    public EmailObject(String emailSender, String recipientAddress, String subject, String body) {
        this.messageSender = emailSender;
        this.messageReceiver = recipientAddress;
        this.messageTitle = subject;
        this.messageBody = body;
    }
}
