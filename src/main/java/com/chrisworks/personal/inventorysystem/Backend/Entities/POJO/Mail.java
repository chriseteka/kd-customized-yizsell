package com.chrisworks.personal.inventorysystem.Backend.Entities.POJO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

/**
 * @author Chris_Eteka
 * @since 11/25/2019
 * @email chriseteka@gmail.com
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Mail {

    @NotNull(message = "message title cannot be null")
    private String messageTitle;

    @NotNull(message = "message title cannot be null")
    @Email(message = "Invalid message sender, please check sender's email address")
    private String messageSender;

    @NotNull(message = "message title cannot be null")
    @Email(message = "Invalid message receiver, please check receiver's email address")
    private String messageReceiver;

    @NotNull(message = "message title cannot be null")
    private String messageBody;
}
