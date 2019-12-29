package com.chrisworks.personal.inventorysystem.Backend.Utility.Events.Listeners;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.BusinessOwner;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.EmailObject;
import com.chrisworks.personal.inventorysystem.Backend.Services.AuthenticationService;
import com.chrisworks.personal.inventorysystem.Backend.Services.MailServices;
import com.chrisworks.personal.inventorysystem.Backend.Utility.Events.OnRegistrationCompleteEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;


@Component
public class RegistrationListener implements ApplicationListener<OnRegistrationCompleteEvent> {

    private final AuthenticationService authenticationService;

    private final MailServices mailServices;

    @Value("${email.sender}") private String emailSender;

    @Autowired
    public RegistrationListener(AuthenticationService authenticationService, MailServices mailServices) {
        this.authenticationService = authenticationService;
        this.mailServices = mailServices;
    }

    @Override
    public void onApplicationEvent(OnRegistrationCompleteEvent event) {
        this.confirmRegistration(event);
    }

    private void confirmRegistration(OnRegistrationCompleteEvent event) {

        BusinessOwner businessOwner = event.getBusinessOwner();
        String token = String.valueOf(System.currentTimeMillis()).substring(6, 12);
        authenticationService.createVerificationToken(businessOwner, token);

        String recipientAddress = businessOwner.getBusinessOwnerEmail();
        String subject = "Registration Confirmation";
        String message = "Confirm your registration by copying the following token and pasting where required: ";
        String body = message + token;

        EmailObject emailObject = new EmailObject(emailSender, recipientAddress, subject, body);

        mailServices.sendAutomatedEmail(emailObject);
    }
}
