package org.hartford.swasthraksha.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

        /**
         * 409 Conflict — business rule violations (waiting period, wrong status, etc.)
         */
        @ExceptionHandler(IllegalStateException.class)
        public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
                return buildError(HttpStatus.CONFLICT, ex.getMessage());
        }

        /** 400 Bad Request — bad input / resource not found */
        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
                return buildError(HttpStatus.BAD_REQUEST, ex.getMessage());
        }

        /**
         * 400 Bad Request — any other unchecked runtime exception with a user-facing
         * message
         */
        @ExceptionHandler(RuntimeException.class)
        public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex) {
                return buildError(HttpStatus.BAD_REQUEST, ex.getMessage());
        }

        private ResponseEntity<Map<String, Object>> buildError(HttpStatus status, String message) {
                Map<String, Object> body = new LinkedHashMap<>();
                body.put("timestamp", Instant.now().toString());
                body.put("status", status.value());
                body.put("error", status.getReasonPhrase());
                body.put("message", message != null ? message : "An unexpected error occurred");
                return ResponseEntity.status(status).body(body);
        }
}
