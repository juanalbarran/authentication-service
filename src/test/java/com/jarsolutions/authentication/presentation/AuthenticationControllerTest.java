package com.jarsolutions.authentication.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jarsolutions.authentication.application.AuthenticationService;
import com.jarsolutions.authentication.application.JwtService;
import com.jarsolutions.authentication.application.input.LoginInput;
import com.jarsolutions.authentication.application.input.RefreshInput;
import com.jarsolutions.authentication.application.input.RegisterInput;
import com.jarsolutions.authentication.application.output.LoginOutput;
import com.jarsolutions.authentication.application.output.RegisterOutput;
import com.jarsolutions.authentication.application.output.TokenOutput;
import com.jarsolutions.authentication.application.output.UserOutput;
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
}
