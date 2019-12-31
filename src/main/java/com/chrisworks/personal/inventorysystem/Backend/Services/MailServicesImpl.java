package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.EmailAttachments;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.EmailObject;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.sendgrid.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class MailServicesImpl implements MailServices {

    private final SendGrid sendGridClient;

    @Autowired
    public MailServicesImpl(SendGrid sendGridClient) {
        this.sendGridClient = sendGridClient;
    }


    @Override
    public void sendAutomatedEmail(EmailObject emailObject) {

        Response response = send(emailObject.getMessageSender(), emailObject.getMessageReceiver(),
                emailObject.getMessageTitle(), new Content("text/plain", emailObject.getMessageBody()),
                Collections.emptyList());
        System.out.println("Status Code: " + response.getStatusCode() + ", Body: " + response.getBody() + ", Headers: "
                + response.getHeaders());
    }

    @Override
    public Response sendEmailToAnyUser(EmailObject emailObject) {

        List<Attachments> attachmentsList = new ArrayList<>();

        List<EmailAttachments> emailAttachments = emailObject.getAttachments();

        //Include attachment if any is found
        if (!emailAttachments.isEmpty()){

            for (EmailAttachments mailAttachment: emailAttachments) {

                Attachments attachments = new Attachments();
                attachments.setContent(mailAttachment.getAttachment());
                attachments.setType(mailAttachment.getAttachmentType());
                attachments.setDisposition("attachment");
                attachments.setFilename(mailAttachment.getFileName());
                attachments.setContentId(mailAttachment.getFileName());

                attachmentsList.add(attachments);
            }
        }
        Response response = send(emailObject.getMessageSender(), emailObject.getMessageReceiver(),
                emailObject.getMessageTitle(), new Content("text/plain", emailObject.getMessageBody()),
                attachmentsList);
        System.out.println("Status Code: " + response.getStatusCode() + ", Body: " + response.getBody() + ", Headers: "
                + response.getHeaders());


        return response;
    }

    private Response send(String from, String to, String subject, Content content, List<Attachments> attachments) {

        Mail mail = new Mail(new Email(from), subject, new Email(to), content);
        if (!attachments.isEmpty())
            attachments.forEach(mail::addAttachments);
        mail.setReplyTo(new Email(from));
        Request request = new Request();
        Response response = null;
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            response = this.sendGridClient.api(request);
        } catch (IOException ex) {

            throw new InventoryAPIOperationException("Network error",
                    "Network error, please check your internet connection", null);
        }

        return response;

    }
}
