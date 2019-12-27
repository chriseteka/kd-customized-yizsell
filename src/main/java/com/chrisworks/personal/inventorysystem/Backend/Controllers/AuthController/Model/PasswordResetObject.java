package com.chrisworks.personal.inventorysystem.Backend.Controllers.AuthController.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

/**
 * @author Chris_Eteka
 * @since 12/16/2019
 * @email chriseteka@gmail.com
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetObject {

    @NotEmpty(message = "newPassword field cannot be empty")
    @Size(min = 4, message = "newPassword field must contain at least four characters")
    private String newPassword;
}
