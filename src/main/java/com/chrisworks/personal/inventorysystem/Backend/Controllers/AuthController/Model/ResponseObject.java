package com.chrisworks.personal.inventorysystem.Backend.Controllers.AuthController.Model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Chris_Eteka
 * @since 12/6/2019
 * @email chriseteka@gmail.com
 */
@Data
@AllArgsConstructor
public class ResponseObject {

    private boolean success;

    private String token;
}
