package com.jarsolutions.authentication.presentation.exception;

import com.jarsolutions.authentication.domain.exception.UserAlreadyExistsException;
import com.jarsolutions.authentication.presentation.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import javax.naming.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(UserAlreadyExistsException.class)
  public ResponseEntity<ErrorResponse> handleUserAlreadyExists(
      UserAlreadyExistsException exception) {
    log.warn("Registration failed - Conflict: {}", exception.getMessage());
    ErrorResponse response =
        new ErrorResponse(
            HttpStatus.CONFLICT.value(), "Conflict", exception.getMessage(), Instant.now());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException exception) {
    log.warn("Invalid or expired token usage attempted: {}", exception.getMessage());
    ErrorResponse response =
        new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(), "Bad Request", exception.getMessage(), Instant.now());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler({UsernameNotFoundException.class, AuthenticationException.class})
  public ResponseEntity<ErrorResponse> handleAuthenticationErrors(
      RuntimeException exception, HttpServletRequest request) {
    String attackerIp = getClientIp(request);
    log.warn("Failed Login Attempt from IP: {} - Reason: {}", attackerIp, exception.getMessage());
    ErrorResponse response =
        new ErrorResponse(
            HttpStatus.UNAUTHORIZED.value(),
            "Unauthorized",
            "Invalid username or password",
            Instant.now());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
  }

  private String getClientIp(HttpServletRequest request) {
    String xfHeader = request.getHeader("X-Forwarded-For");
    if (xfHeader != null && !xfHeader.isEmpty()) {
      return xfHeader.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }
}
