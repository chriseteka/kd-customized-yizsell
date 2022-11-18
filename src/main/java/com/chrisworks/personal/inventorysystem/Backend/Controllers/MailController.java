package com.chrisworks.personal.inventorysystem.Backend.Controllers;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.EmailObject;
import com.chrisworks.personal.inventorysystem.Backend.Services.MailServices;
import com.sendgrid.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author Chris_Eteka
 * @since 12/30/2019
 * @email chriseteka@gmail.com
 */
@RestController
@RequestMapping("/mail")
public class MailController {

    private final MailServices mailServices;

    @Autowired
    public MailController(MailServices mailServices) {
        this.mailServices = mailServices;
    }

    @PostMapping(path = "send", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> sendMail(@RequestBody @Valid EmailObject emailObject){

        Response response = mailServices.sendEmailToAnyUser(emailObject);

        return ResponseEntity
                .status(HttpStatus.valueOf(response.getStatusCode()))
                .body(response.getBody());
    }
}
