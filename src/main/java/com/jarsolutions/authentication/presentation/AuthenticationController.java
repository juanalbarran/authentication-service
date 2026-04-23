package com.jarsolutions.authentication.presentation;

import com.jarsolutions.authentication.application.AuthenticationService;
import com.jarsolutions.authentication.application.JwtService;
import com.jarsolutions.authentication.application.input.RegisterInput;
import com.jarsolutions.authentication.application.output.RegisterOutput;
import com.jarsolutions.authentication.presentation.request.LoginRequest;
import com.jarsolutions.authentication.presentation.request.RegisterRequest;
import com.jarsolutions.authentication.presentation.response.AuthResponse;
import com.jarsolutions.authentication.presentation.response.RegisterResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthenticationController {

  private final AuthenticationService authenticationService;
  private final JwtService jwtService;

  public AuthenticationController(
      AuthenticationService authenticationService, JwtService jwtService) {
    this.authenticationService = authenticationService;
    this.jwtService = jwtService;
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
    return null;
  }

  @PostMapping("/register")
  public ResponseEntity<RegisterResponse> register(
      @Valid @RequestBody RegisterRequest registerRequest) {
    RegisterInput registerInput =
        new RegisterInput(
            registerRequest.username(), registerRequest.password(), registerRequest.deviceInfo());
    RegisterOutput registerOutput = authenticationService.register(registerInput);
    RegisterResponse registerResponse =
        new RegisterResponse(registerOutput.tokenOutput().accessToken());
    ResponseCookie responseCookie =
        ResponseCookie.from("refresh_token", registerOutput.tokenOutput().refreshToken())
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(jwtService.getRefreshTokenExpiration() / 1000)
            .build();

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
        .body(registerResponse);
  }
}
