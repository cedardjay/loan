package com.finance.loan.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OurException.class)
    public ResponseEntity<ErrorResponse> handleOurException(OurException e) {
        return ResponseEntity.status(e.getStatus()).body(
                ErrorResponse.builder()
                        .status(e.getStatus())
                        .message(e.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException e) {
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return ResponseEntity.status(400).body(
                ErrorResponse.builder()
                        .status(400)
                        .message(message)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException e) {
        return ResponseEntity.status(403).body(
                ErrorResponse.builder()
                        .status(403)
                        .message(e.getMessage())  // Uses the exception's message
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception e) {
        return ResponseEntity.status(500).body(
                ErrorResponse.builder()
                        .status(500)
                        .message("An unexpected error occurred: " + e.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }
}
