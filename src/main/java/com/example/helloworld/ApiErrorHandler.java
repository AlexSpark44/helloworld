package com.example.helloworld;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiErrorHandler {
  @ExceptionHandler(VersionConflictException.class)
  public ResponseEntity<Map<String, Object>> handleVersionConflict(VersionConflictException ex) {
    Map<String, Object> body = Map.of(
        "error", "CART_VERSION_CONFLICT",
        "message", ex.getMessage(),
        "currentVersion", ex.getCurrentVersion()
    );
    return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
  }

  @ExceptionHandler(CartNotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleNotFound(CartNotFoundException ex) {
    Map<String, Object> body = Map.of(
        "error", "CART_NOT_FOUND",
        "message", ex.getMessage()
    );
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
  }
}
