package com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.ExceptionResponse;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

/**
 * @author Chris_Eteka
 * @since 12/4/2019
 * @email chriseteka@gmail.com
 */
@Data
@AllArgsConstructor
public class ExceptionResponse {

    private Date timestamp;

    private int statusCode;

    private Object message;

    private String details;
}
