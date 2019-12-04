package com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.ExceptionResponse;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Chris_Eteka
 * @since 12/4/2019
 * @email chriseteka@gmail.com
 */
@Data
@AllArgsConstructor
public class ErrorDetailsObject {

    private String errorField;

    private String errorMessage;
}
