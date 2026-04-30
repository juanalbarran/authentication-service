package com.jarsolutions.authentication.presentation.exception;

import com.jarsolutions.authentication.domain.exception.UserAlreadyExistsException;
import com.jarsolutions.authentication.presentation.response.ErrorResponse;
import java.time.Instant;
import javax.naming.AuthenticationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(UserAlreadyExistsException.class)
  public ResponseEntity<ErrorResponse> handleUserAlreadyExists(
      UserAlreadyExistsException exception) {
    ErrorResponse response =
        new ErrorResponse(
            HttpStatus.CONFLICT.value(), "Conflict", exception.getMessage(), Instant.now());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException exception) {
    ErrorResponse response =
        new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(), "Bad Request", exception.getMessage(), Instant.now());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler({UsernameNotFoundException.class, AuthenticationException.class})
  public ResponseEntity<ErrorResponse> handleAuthenticationErrors(RuntimeException exception) {
    ErrorResponse response =
        new ErrorResponse(
            HttpStatus.UNAUTHORIZED.value(),
            "Unauthorized",
            "Invalid username or password",
            Instant.now());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
  }
}
