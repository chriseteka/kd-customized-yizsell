package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.EmailObject;
import com.sendgrid.Response;

/**
 * @author Chris_Eteka
 * @since 12/2/2019
 * @email chriseteka@gmail.com
 */
public interface MailServices {

    //EmailObject invoices as stock are sold to the business owner
    //Allow business owner mail suppliers and customers

    void sendAutomatedEmail(EmailObject emailObject);

    Response sendEmailToAnyUser(EmailObject emailObject);
}
