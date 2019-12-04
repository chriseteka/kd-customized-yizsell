package com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.ExceptionHandlers;

import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.ExceptionResponse.ExceptionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Date;
import java.util.Set;

/**
 * @author Chris_Eteka
 * @since 12/4/2019
 * @email chriseteka@gmail.com
 */
//This was added due to @Validated annotation called on some controller classes
@RestControllerAdvice
@Component
public class CustomControllerExceptionHandler {

    /**
     * handle controller methods parameter validation exceptions
     *
     * @param exception ex
     * @return wrapped result
     */
    @ExceptionHandler
    @ResponseBody
    @ResponseStatus(HttpStatus.PRECONDITION_FAILED)
    public ExceptionResponse controllerValidation(ConstraintViolationException exception) {

        Set<ConstraintViolation<?>> violations = exception.getConstraintViolations();
        StringBuilder builder = new StringBuilder();
        for (ConstraintViolation<?> violation : violations) {
            builder.append(violation.getMessage());
            break;
        }
        return new ExceptionResponse(new Date(), HttpStatus.PRECONDITION_FAILED.value(), exception.getMessage(), builder.toString());
    }
}
