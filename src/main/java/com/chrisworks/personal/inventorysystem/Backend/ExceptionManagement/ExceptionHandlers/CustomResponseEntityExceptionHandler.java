package com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.ExceptionHandlers;

import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.ExceptionResponse.ErrorDetailsObject;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.ExceptionResponse.ExceptionResponse;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Chris_Eteka
 * @since 12/4/2019
 * @email chriseteka@gmail.com
 */
@ControllerAdvice
@RestController
public class CustomResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(Exception.class)
    private ResponseEntity<?> handleAllExceptions(Exception ex, WebRequest request){

        ExceptionResponse exceptionResponse = new ExceptionResponse
                (new Date(), HttpStatus.PRECONDITION_FAILED.value(), ex.getMessage(), request.getDescription(false));

        return new ResponseEntity<>(exceptionResponse, HttpStatus.PRECONDITION_FAILED);
    }

    @ExceptionHandler(InventoryAPIResourceNotFoundException.class)
    private ResponseEntity<?> handleInventoryResourceNotFoundExceptions(InventoryAPIExceptions ex, WebRequest request){

        ExceptionResponse exceptionResponse = new ExceptionResponse
                (new Date(), HttpStatus.NOT_FOUND.value(), ex.getDefaultUserMessage(), request.getDescription(false));

        return new ResponseEntity<>(exceptionResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InventoryAPIDataValidationException.class)
    private ResponseEntity<?> handleInventoryDataValidationExceptions(InventoryAPIExceptions ex, WebRequest request){

        ExceptionResponse exceptionResponse = new ExceptionResponse
                (new Date(), HttpStatus.BAD_REQUEST.value(), ex.getDefaultUserMessage(), request.getDescription(false));

        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InventoryAPIDuplicateEntryException.class)
    private ResponseEntity<?> handleInventoryDuplicateEntryExceptions(InventoryAPIExceptions ex, WebRequest request){

        ExceptionResponse exceptionResponse = new ExceptionResponse
                (new Date(), HttpStatus.CONFLICT.value(), ex.getDefaultUserMessage(), request.getDescription(false));

        return new ResponseEntity<>(exceptionResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InventoryAPIFunctionNotSupportedException.class)
    private ResponseEntity<?> handleFunctionNotSupportedExceptions(InventoryAPIExceptions ex, WebRequest request){

        ExceptionResponse exceptionResponse = new ExceptionResponse
                (new Date(), HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(), ex.getDefaultUserMessage(), request.getDescription(false));

        return new ResponseEntity<>(exceptionResponse, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(InventoryAPIOperationException.class)
    private ResponseEntity<?> handleOperationExceptions(InventoryAPIExceptions ex, WebRequest request){

        ExceptionResponse exceptionResponse = new ExceptionResponse
                (new Date(), HttpStatus.PRECONDITION_FAILED.value(), ex.getDefaultUserMessage(), request.getDescription(false));

        return new ResponseEntity<>(exceptionResponse, HttpStatus.PRECONDITION_FAILED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public  final ResponseEntity<?> handleAuthorizationException(AccessDeniedException ex, WebRequest request) {

        ExceptionResponse exceptionResponse = new
                ExceptionResponse(new Date(), HttpStatus.FORBIDDEN.value(), ex.getMessage(), request.getDescription(false));

        return new ResponseEntity<>(exceptionResponse, HttpStatus.FORBIDDEN);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        List<ErrorDetailsObject> errorObjectList =
                ex.getBindingResult().getFieldErrors()
                .stream()
                .filter(fieldError ->
                        (!fieldError.getField().isEmpty() && fieldError.getDefaultMessage() != null))
                .map(fieldError ->
                        new ErrorDetailsObject(fieldError.getField(), fieldError.getDefaultMessage())
                ).collect(Collectors.toList());

        ExceptionResponse exceptionResponse = new ExceptionResponse
                (new Date(), status.value(), errorObjectList, request.getDescription(false));

        return new ResponseEntity<>(exceptionResponse, status);
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        ExceptionResponse exceptionResponse = new ExceptionResponse
                (new Date(), status.value(), ex.getMessage(), request.getDescription(false));

        return new ResponseEntity<>(exceptionResponse, status);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        ExceptionResponse exceptionResponse = new ExceptionResponse
                (new Date(), status.value(), ex.getMessage(), request.getDescription(false));

        return new ResponseEntity<>(exceptionResponse, status);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        ExceptionResponse exceptionResponse = new ExceptionResponse
                (new Date(), status.value(), ex.getMessage(), request.getDescription(false));

        return new ResponseEntity<>(exceptionResponse, status);
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        ExceptionResponse exceptionResponse = new ExceptionResponse
                (new Date(), status.value(), ex.getMessage(), request.getDescription(false));

        return new ResponseEntity<>(exceptionResponse, status);
    }

    //DB error, remove this before prod
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> SQLDataViolationExceptionHandler(DataIntegrityViolationException ex, WebRequest request) {

        ExceptionResponse exceptionResponse = new
                ExceptionResponse(new Date(), HttpStatus.CONFLICT.value(),
                "DB VIOLATION ERROR: You maybe trying to delete a data holding other data as children. \n\n" + ex.getMessage(),
                request.getDescription(false));

        return new ResponseEntity<>(exceptionResponse, HttpStatus.CONFLICT);

    }
}
