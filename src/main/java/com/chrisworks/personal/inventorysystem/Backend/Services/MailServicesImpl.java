package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.EmailObject;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.sendgrid.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class MailServicesImpl implements MailServices {

    private final SendGrid sendGridClient;

    @Autowired
    public MailServicesImpl(SendGrid sendGridClient) {
        this.sendGridClient = sendGridClient;
    }


    @Override
    public void sendEmail(EmailObject emailObject) {

        Response response = send(emailObject.getMessageSender(), emailObject.getMessageReceiver(),
                emailObject.getMessageTitle(), new Content("text/plain", emailObject.getMessageBody()));
        System.out.println("Status Code: " + response.getStatusCode() + ", Body: " + response.getBody() + ", Headers: "
                + response.getHeaders());
    }

    private Response send(String from, String to, String subject, Content content) {

        Mail mail = new Mail(new Email(from), subject, new Email(to), content);
        mail.setReplyTo(new Email(from));
        Request request = new Request();
        Response response = null;
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            response = this.sendGridClient.api(request);
        } catch (IOException ex) {

            ex.printStackTrace();
            throw new InventoryAPIOperationException(String.valueOf(response.getStatusCode()), response.getBody(), null);
        }

        return response;

    }
}
