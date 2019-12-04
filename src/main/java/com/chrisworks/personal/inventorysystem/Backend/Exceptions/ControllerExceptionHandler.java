package com.chrisworks.personal.inventorysystem.Backend.Exceptions;

import com.chrisworks.personal.inventorysystem.Backend.Exceptions.ExceptionResponse.ExceptionResponse;
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

@RestControllerAdvice
@Component
public class ControllerExceptionHandler {

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
        return new ExceptionResponse(new Date(), HttpStatus.PRECONDITION_FAILED.toString(), builder.toString());
    }
}
