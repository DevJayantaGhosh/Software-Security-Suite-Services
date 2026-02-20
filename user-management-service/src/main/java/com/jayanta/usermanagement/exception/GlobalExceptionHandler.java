package com.jayanta.usermanagement.exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserException.class)
    public ResponseEntity<ApiError> handleUserException(UserException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiError.builder()
                        .status(HttpStatus.BAD_REQUEST)
                        .message(ex.getMessage())
                        .code(ex.getCode())
                        .field(ex.getField())
                        .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(field, message);
        });
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiError.builder()
                        .status(HttpStatus.FORBIDDEN)
                        .message("Access denied")
                        .code("ACCESS_DENIED")
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiError.builder()
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .message("Internal server error")
                        .code("INTERNAL_ERROR")
                        .build());
    }
}
