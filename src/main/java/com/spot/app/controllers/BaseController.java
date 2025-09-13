package com.spot.app.controllers;

import com.spot.app.dtos.ErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public abstract class BaseController {

    protected ResponseEntity<ErrorResponseDTO> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponseDTO(HttpStatus.UNAUTHORIZED.value(), "UNAUTHORIZED", "Invalid or expired session token"));
    }
    protected ResponseEntity<ErrorResponseDTO> unauthorized(String msg) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponseDTO(HttpStatus.UNAUTHORIZED.value(), "UNAUTHORIZED", msg));
    }

    protected ResponseEntity<ErrorResponseDTO> forbidden(String msg) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponseDTO(HttpStatus.FORBIDDEN.value(), "FORBIDDEN", msg));
    }

    protected ResponseEntity<ErrorResponseDTO> notFound(String msg) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDTO(HttpStatus.NOT_FOUND.value(), "NOT_FOUND", msg));
    }

    protected ResponseEntity<ErrorResponseDTO> badRequest(String msg) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDTO(HttpStatus.BAD_REQUEST.value(), "BAD_REQUEST", msg));
    }

    protected ResponseEntity<ErrorResponseDTO> conflict(String msg) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponseDTO(HttpStatus.CONFLICT.value(), "CONFLICT", msg));
    }
}