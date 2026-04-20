package com.jarsolutions.authentication.presentation;

import com.jarsolutions.authentication.presentation.request.LoginRequest;
import com.jarsolutions.authentication.presentation.request.RegisterRequest;
import com.jarsolutions.authentication.presentation.response.AuthResponse;
import com.jarsolutions.authentication.presentation.response.RegisterResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthenticationController {

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
    return null;
  }

  @PostMapping("/register")
  public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest RegisterRequest) {
    return null;
  }
}
