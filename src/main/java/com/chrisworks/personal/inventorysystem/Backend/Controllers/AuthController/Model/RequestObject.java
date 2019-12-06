package com.chrisworks.personal.inventorysystem.Backend.Controllers.AuthController.Model;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Seller;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

/**
 * @author Chris_Eteka
 * @since 11/27/2019
 * @email chriseteka@gmail.com
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestObject {

    @Email(message = "Invalid Email Address Entered")
    @NotEmpty(message = "Username cannot be empty")
    @Size(min = 3, message = "Username should be at least three characters")
    private String username;

    @NotEmpty(message = "Password cannot be empty")
    @Size(min = 4, message = "Password should be at least four characters")
    private String password;
}
