package com.jarsolutions.authentication.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.jarsolutions.authentication.application.input.LoginInput;
import com.jarsolutions.authentication.application.input.RefreshInput;
import com.jarsolutions.authentication.application.input.RegisterInput;
import com.jarsolutions.authentication.application.output.LoginOutput;
import com.jarsolutions.authentication.application.output.RegisterOutput;
import com.jarsolutions.authentication.application.output.TokenOutput;
import com.jarsolutions.authentication.domain.entity.User;
import com.jarsolutions.authentication.domain.entity.UserSession;
import com.jarsolutions.authentication.domain.exception.UserAlreadyExistsException;
import com.jarsolutions.authentication.domain.repository.UserRepository;
import com.jarsolutions.authentication.domain.repository.UserSessionRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private UserSessionRepository userSessionRepository;
  @Mock private AuthenticationManager authenticationManager;
  @Mock private JwtService jwtService;
  @Mock private PasswordEncoder passwordEncoder;

  @InjectMocks private AuthenticationService authenticationService;

  @Test
  void register_WhenUserDoesNotExists_ShouldSuccessfullyRegisterUser() {
    RegisterInput input = new RegisterInput("juan.albarran", "password", "macbook:juan");
    User savedUser = new User("juan.albarran", "hashedPassword");
    savedUser.setId(1L);

    when(userRepository.existsByUsername("juan.albarran")).thenReturn(false);
    when(passwordEncoder.encode("password")).thenReturn("hashedPassword");
    when(userRepository.save(any(User.class))).thenReturn(savedUser);
    when(jwtService.generateAccessToken("juan.albarran")).thenReturn("mock-access-token");
    when(jwtService.generateRefreshToken("juan.albarran")).thenReturn("mock-refresh-token");

    when(jwtService.getRefreshTokenExpiration()).thenReturn(604800000L);

    RegisterOutput output = authenticationService.register(input);

    assertNotNull(output);
    assertEquals("mock-access-token", output.tokenOutput().accessToken());
    assertEquals("mock-refresh-token", output.tokenOutput().refreshToken());
    assertEquals("juan.albarran", output.userOutput().username());
    assertEquals(1L, output.userOutput().id());

    verify(userRepository, times(1)).save(any(User.class));
    verify(userSessionRepository, times(1)).save(any());
  }

  @Test
  void register_WhenUserAlreadyExists_ShouldThrowException() {
    RegisterInput input = new RegisterInput("juan.albarran", "password", "macbook:juan");

    when(userRepository.existsByUsername("juan.albarran")).thenReturn(true);

    UserAlreadyExistsException exception =
        assertThrows(UserAlreadyExistsException.class, () -> authenticationService.register(input));

    assertEquals("User juan.albarran already exists.", exception.getMessage());

    verify(userRepository, never()).save(any(User.class));
    verify(userSessionRepository, never()).save(any());
  }

  @Test
  void login_WhenCredentialsAreValid_ShouldSuccessfullyLogin() {
    LoginInput input = new LoginInput("juan.albarran", "superSecretPassword", "macbook:office");
    User existingUser = new User("juan.albarran", "hashedPassword");
    existingUser.setId(1L);

    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(
            new UsernamePasswordAuthenticationToken("juan.albarran", "superSecretPassword"));
    when(userRepository.findByUsername("juan.albarran")).thenReturn(Optional.of(existingUser));
    when(jwtService.generateAccessToken("juan.albarran")).thenReturn("mock-access-token");
    when(jwtService.generateRefreshToken("juan.albarran")).thenReturn("mock-refresh-token");
    when(jwtService.getRefreshTokenExpiration()).thenReturn(604800000L);

    LoginOutput output = authenticationService.login(input);

    assertNotNull(output);
    assertEquals("mock-access-token", output.tokenOutput().accessToken());
    assertEquals("mock-refresh-token", output.tokenOutput().refreshToken());
    assertEquals("juan.albarran", output.userOutput().username());
    assertEquals(1L, output.userOutput().id());

    verify(userSessionRepository, times(1)).save(any(UserSession.class));
  }

  @Test
  void login_WhenUserNotFoundInDatabase_ShouldThrowException() {
    LoginInput input = new LoginInput("juan.albarran", "superSecretPassword", "macbook:office");

    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(new UsernamePasswordAuthenticationToken("juan.albarran", "password"));
    when(userRepository.findByUsername("juan.albarran")).thenReturn(Optional.empty());
    UsernameNotFoundException exception =
        assertThrows(UsernameNotFoundException.class, () -> authenticationService.login(input));
    assertEquals("There is no user with that username.", exception.getMessage());

    verify(jwtService, never()).generateAccessToken(anyString());
    verify(userSessionRepository, never()).save(any());
  }

  @Test
  void refresh_WhenTokenIsValidAndUnexpired_ShouldReturnNewTokensAndRotateSession() {
    RefreshInput input = new RefreshInput("valid-old-token", "macbook:office");
    User existingUser = new User("juan.albarran", "hashedPassword");

    UserSession activeSession =
        new UserSession(
            "valid-old-token", "macbook", Instant.now().plusSeconds(3600), existingUser);

    when(userSessionRepository.findByToken("valid-old-token"))
        .thenReturn(Optional.of(activeSession));
    when(jwtService.generateAccessToken("juan.albarran")).thenReturn("new-acess-token");
    when(jwtService.generateRefreshToken("juan.albarran")).thenReturn("new-refresh-token");
    when(jwtService.getRefreshTokenExpiration()).thenReturn(604800000L);

    TokenOutput output = authenticationService.refresh(input);

    assertNotNull(output);
    assertEquals("new-acess-token", output.accessToken());
    assertEquals("new-refresh-token", output.refreshToken());

    verify(userSessionRepository, times(1)).delete(activeSession);
    verify(userSessionRepository, times(1)).save(any(UserSession.class));
  }

  @Test
  void refresh_WhenTokenIsExpired_ShouldDeleteSessionAndThrowException() {
    RefreshInput input = new RefreshInput("expired-token", "macbook");
    User existingUser = new User("juan.albarran", "superSecretPassword");

    UserSession expiredSession =
        new UserSession("expired-token", "macbook", Instant.now().minusSeconds(3600), existingUser);

    when(userSessionRepository.findByToken("expired-token"))
        .thenReturn(Optional.of(expiredSession));

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> authenticationService.refresh(input));

    assertEquals("Refresh token has expired. Please log in again.", exception.getMessage());

    verify(userSessionRepository, times(1)).delete(expiredSession);
    verify(jwtService, never()).generateAccessToken(anyString());
    verify(userSessionRepository, never()).save(any(UserSession.class));
  }

  @Test
  void refresh_WhenTokenIsNotFoundInDatabase_ShouldThrowException() {
    RefreshInput input = new RefreshInput("fake-token", "macbook");

    when(userSessionRepository.findByToken("fake-token")).thenReturn(Optional.empty());

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> authenticationService.refresh(input));

    assertEquals("Invalid refresh token", exception.getMessage());

    verify(userSessionRepository, never()).delete(any());
    verify(jwtService, never()).generateAccessToken(anyString());
  }
}
