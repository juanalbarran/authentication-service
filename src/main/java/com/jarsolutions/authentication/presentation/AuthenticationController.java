package com.jarsolutions.authentication.presentation;

import com.jarsolutions.authentication.application.AuthenticationService;
import com.jarsolutions.authentication.application.JwtService;
import com.jarsolutions.authentication.application.input.LoginInput;
import com.jarsolutions.authentication.application.input.LogoutInput;
import com.jarsolutions.authentication.application.input.RefreshInput;
import com.jarsolutions.authentication.application.input.RegisterInput;
import com.jarsolutions.authentication.application.output.LoginOutput;
import com.jarsolutions.authentication.application.output.RegisterOutput;
import com.jarsolutions.authentication.application.output.TokenOutput;
import com.jarsolutions.authentication.presentation.mapper.AuthenticationMapper;
import com.jarsolutions.authentication.presentation.request.LoginRequest;
import com.jarsolutions.authentication.presentation.request.RegisterRequest;
import com.jarsolutions.authentication.presentation.response.LoginResponse;
import com.jarsolutions.authentication.presentation.response.RegisterResponse;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
    LoginInput loginInput = AuthenticationMapper.toLoginInput(loginRequest);
    LoginOutput loginOutput = authenticationService.login(loginInput);
    LoginResponse loginResponse = AuthenticationMapper.toLoginResponse(loginOutput);
    ResponseCookie responseCookie =
        ResponseCookie.from("refresh_token", loginOutput.tokenOutput().refreshToken())
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(jwtService.getRefreshTokenExpiration() / 1000)
            .build();

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
        .body(loginResponse);
  }

  @PostMapping("/register")
  public ResponseEntity<RegisterResponse> register(
      @Valid @RequestBody RegisterRequest registerRequest) {
    RegisterInput registerInput = AuthenticationMapper.toRegisterInput(registerRequest);
    RegisterOutput registerOutput = authenticationService.register(registerInput);
    RegisterResponse registerResponse = AuthenticationMapper.toRegisterResponse(registerOutput);
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

  @PostMapping("/refresh")
  public ResponseEntity<Map<String, String>> refresh(
      @CookieValue(name = "refresh_token") String refreshToken,
      @RequestHeader(value = "User-Agent", defaultValue = "unknown") String deviceInfo) {
    RefreshInput input = new RefreshInput(refreshToken, deviceInfo);
    TokenOutput tokens = authenticationService.refresh(input);

    long cookieExpirationInSeconds = jwtService.getRefreshTokenExpiration() / 1000;
    ResponseCookie cookie =
        ResponseCookie.from("refresh_token", tokens.refreshToken())
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(cookieExpirationInSeconds)
            .build();
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, cookie.toString())
        .body(Map.of("accessToken", tokens.accessToken()));
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(
      @CookieValue(name = "refresh_token") String refreshToken,
      @RequestHeader(value = "User-Agent", defaultValue = "unknown") String deviceInfo) {
    LogoutInput logoutInput = new LogoutInput(refreshToken, deviceInfo);
    authenticationService.logout(logoutInput);
    ResponseCookie blankCookie =
        ResponseCookie.from("refresh_token", "")
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(0)
            .build();
    return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, blankCookie.toString()).build();
  }
}
