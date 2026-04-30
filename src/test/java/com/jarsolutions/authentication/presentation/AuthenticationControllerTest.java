package com.jarsolutions.authentication.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jarsolutions.authentication.application.AuthenticationService;
import com.jarsolutions.authentication.application.JwtService;
import com.jarsolutions.authentication.application.input.LoginInput;
import com.jarsolutions.authentication.application.input.LogoutInput;
import com.jarsolutions.authentication.application.input.RefreshInput;
import com.jarsolutions.authentication.application.input.RegisterInput;
import com.jarsolutions.authentication.application.output.LoginOutput;
import com.jarsolutions.authentication.application.output.RegisterOutput;
import com.jarsolutions.authentication.application.output.TokenOutput;
import com.jarsolutions.authentication.application.output.UserOutput;
import com.jarsolutions.authentication.domain.exception.UserAlreadyExistsException;
import com.jarsolutions.authentication.presentation.request.LoginRequest;
import com.jarsolutions.authentication.presentation.request.RegisterRequest;
import jakarta.servlet.http.Cookie;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthenticationController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthenticationControllerTest {
  @Autowired private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @MockitoBean private AuthenticationService authenticationService;

  @MockitoBean private JwtService jwtService;

  @Test
  void login_WhenValidRequest_ShouldReturn200AndCookies() throws Exception {
    LoginRequest request =
        new LoginRequest("juan.albarran", "superSecretPassword", "macbook:office");

    TokenOutput tokenOutput = new TokenOutput("mock-access-token", "mock-refresh-token");
    UserOutput userOutput = new UserOutput(1L, "juan.albarran");
    LoginOutput loginOutput = new LoginOutput(tokenOutput, userOutput);

    when(authenticationService.login(any(LoginInput.class))).thenReturn(loginOutput);
    when(jwtService.getRefreshTokenExpiration()).thenReturn(604800000L);

    mockMvc
        .perform(
            post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.username").value("juan.albarran"))
        .andExpect(cookie().exists("refresh_token"))
        .andExpect(cookie().value("refresh_token", "mock-refresh-token"))
        .andExpect(cookie().httpOnly("refresh_token", true))
        .andExpect(cookie().secure("refresh_token", true));
  }

  @Test
  void register_WhenValidRequest_ShouldReturn200AndCookies() throws Exception {
    RegisterRequest request =
        new RegisterRequest("juan.albarran", "superSecretPassword", "macbook:office");

    TokenOutput tokenOutput = new TokenOutput("mock-access-token", "mock-refresh-token");
    UserOutput userOutput = new UserOutput(1L, "juan.albarran");
    RegisterOutput registerOutput = new RegisterOutput(tokenOutput, userOutput);

    when(authenticationService.register(any(RegisterInput.class))).thenReturn(registerOutput);
    when(jwtService.getRefreshTokenExpiration()).thenReturn(604800000L);

    mockMvc
        .perform(
            post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("mock-access-token"))
        .andExpect(cookie().exists("refresh_token"))
        .andExpect(cookie().value("refresh_token", "mock-refresh-token"))
        .andExpect(cookie().httpOnly("refresh_token", true))
        .andExpect(cookie().secure("refresh_token", true));
  }

  @Test
  void login_WhenRequestIsInvalid_ShouldReturn400() throws Exception {
    LoginRequest request = new LoginRequest("", "superSecretPassword", "macbook:office");

    mockMvc
        .perform(
            post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void login_WhenUserNotFound_SHouldReturn401ErrorResponse() throws Exception {
    LoginRequest request = new LoginRequest("fake.user", "superSecretPassword", "macbook");
    when(authenticationService.login(any(LoginInput.class)))
        .thenThrow(new UsernameNotFoundException("There is no user with that username"));
    mockMvc
        .perform(
            post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.error").value("Unauthorized"))
        .andExpect(jsonPath("$.message").value("Invalid username or password"))
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  void register_WhenRequestIsInvalid_ShouldReturn400() throws Exception {
    RegisterRequest request = new RegisterRequest("", "superSecretPassword", "macbook:office");

    mockMvc
        .perform(
            post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void refresh_WhenValidCookie_ShouldReturn200AndNewTokens() throws Exception {
    String oldRefreshToken = "valid-old-token";
    TokenOutput newTokens = new TokenOutput("new-access-token", "new-refresh-token");

    when(authenticationService.refresh(any(RefreshInput.class))).thenReturn(newTokens);
    when(jwtService.getRefreshTokenExpiration()).thenReturn(604800000L);

    mockMvc
        .perform(post("/refresh").cookie(new Cookie("refresh_token", oldRefreshToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("new-access-token"))
        .andExpect(header().exists(HttpHeaders.SET_COOKIE))
        .andExpect(
            header()
                .string(
                    HttpHeaders.SET_COOKIE,
                    Matchers.containsString("refresh_token=new-refresh-token")))
        .andExpect(header().string(HttpHeaders.SET_COOKIE, Matchers.containsString("HttpOnly")))
        .andExpect(header().string(HttpHeaders.SET_COOKIE, Matchers.containsString("Secure")));
  }

  @Test
  void refresh_WhenCookieIsMissing_ShouldReturn400() throws Exception {
    mockMvc.perform(post("/refresh")).andExpect(status().isBadRequest());
  }

  @Test
  void register_WhenUserAlreadyExists_ShouldReturn409ErrorResponse() throws Exception {
    RegisterRequest request =
        new RegisterRequest("juan.albarran", "superSecretPassword", "macbook");

    when(authenticationService.register(any(RegisterInput.class)))
        .thenThrow(new UserAlreadyExistsException("User juan.albarran already exists."));

    mockMvc
        .perform(
            post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.status").value(409))
        .andExpect(jsonPath("$.error").value("Conflict"))
        .andExpect(jsonPath("$.message").value("User juan.albarran already exists."))
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  void logout_WhenValidCookie_ShoudReturn200AndClearCookie() throws Exception {
    String activeToken = "valid-refresh-token";

    mockMvc
        .perform(post("/logout").cookie(new Cookie("refresh_token", activeToken)))
        .andExpect(status().isOk())
        .andExpect(header().exists(HttpHeaders.SET_COOKIE))
        .andExpect(
            header().string(HttpHeaders.SET_COOKIE, Matchers.containsString("refresh_token=;")))
        .andExpect(header().string(HttpHeaders.SET_COOKIE, Matchers.containsString("Max-Age=0")))
        .andExpect(header().string(HttpHeaders.SET_COOKIE, Matchers.containsString("HttpOnly")))
        .andExpect(header().string(HttpHeaders.SET_COOKIE, Matchers.containsString("Secure")));
    verify(authenticationService, times(1)).logout(any(LogoutInput.class));
  }

  @Test
  void logout_WhenValidCookieIsMissing_ShouldReturn400() throws Exception {
    mockMvc.perform(post("/logout")).andExpect(status().isBadRequest());
    verify(authenticationService, never()).logout(any());
  }
}
