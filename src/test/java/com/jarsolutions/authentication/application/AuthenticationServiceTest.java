package com.jarsolutions.authentication.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.jarsolutions.authentication.application.input.RegisterInput;
import com.jarsolutions.authentication.application.output.RegisterOutput;
import com.jarsolutions.authentication.domain.entity.User;
import com.jarsolutions.authentication.domain.exception.UserAlreadyExistsException;
import com.jarsolutions.authentication.domain.repository.UserRepository;
import com.jarsolutions.authentication.domain.repository.UserSessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
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
}
