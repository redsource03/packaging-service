package com.globe.gcash.packaging.controller;

import com.globe.gcash.packaging.exception.RequestValidationException;
import com.globe.gcash.packaging.model.response.GenericErrorResponse;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;

@ControllerAdvice
@Slf4j
public class ControllerExceptionGlobalHandler {

    @ExceptionHandler(RequestValidationException.class)
    public ResponseEntity<GenericErrorResponse> handleRequestValidation(RequestValidationException ex) {
        GenericErrorResponse errorResponse = new GenericErrorResponse(
                HttpStatus.BAD_REQUEST.value(),"request.validation.error",
                ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<GenericErrorResponse> handleValidation(WebExchangeBindException ex) {
        GenericErrorResponse errorResponse = new GenericErrorResponse(
                HttpStatus.BAD_REQUEST.value(),"request.binding.error",
                ex.getReason());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<GenericErrorResponse> handle500(Exception ex) {
        log.error(String.format("API CLIENT EXCEPTION:%S",ex.getMessage()));
        GenericErrorResponse errorResponse = new GenericErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),"api.client.error",
                ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
