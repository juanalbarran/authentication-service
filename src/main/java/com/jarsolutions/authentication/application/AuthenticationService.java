package com.jarsolutions.authentication.application;

import com.jarsolutions.authentication.domain.entity.User;
import com.jarsolutions.authentication.domain.entity.UserSession;
import com.jarsolutions.authentication.domain.exception.UserAlreadyExistsException;
import com.jarsolutions.authentication.domain.repository.UserRepository;
import com.jarsolutions.authentication.domain.repository.UserSessionRepository;
import java.time.Instant;
import lombok.Data;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Data
@Service
public class AuthenticationService {

  private final UserRepository userRepository;
  private final UserSessionRepository userSessionRepository;
  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;
  private final PasswordEncoder passwordEncoder;

  public AuthenticationService(
      UserRepository userRepository,
      UserSessionRepository userSessionRepository,
      AuthenticationManager authenticationManager,
      JwtService jwtService,
      PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.authenticationManager = authenticationManager;
    this.jwtService = jwtService;
    this.passwordEncoder = passwordEncoder;
    this.userSessionRepository = userSessionRepository;
  }

  public record UserResult(Long id, String username) {}
  ;

  public record LoginResult(String accessToken, String refreshToken, UserResult userResult) {}
  ;

  public record RegisterResult(String accessToken, String refreshToken, UserResult userResult) {}
  ;

  public LoginResult login(String username, String password, String deviceType) {
    authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

    return null;
  }

  public RegisterResult register(String username, String password, String deviceInfo)
      throws UserAlreadyExistsException {
    if (userRepository.existsByUsername(username)) {
      throw new UserAlreadyExistsException("User " + username + " already exists.");
    }

    String hashedPassword = passwordEncoder.encode(password);

    User user = new User(username, hashedPassword);
    User savedUser = userRepository.save(user);

    String accessToken = jwtService.generateAccessToken(savedUser.getUsername());
    String refreshToken = jwtService.generateRefreshToken(savedUser.getUsername());

    Instant refreshTokenExpiration =
        Instant.now().plusMillis(jwtService.getRefreshTokenExpiration());

    UserSession userSession =
        new UserSession(refreshToken, deviceInfo, refreshTokenExpiration, savedUser);
    UserSession savedUserSession = userSessionRepository.save(userSession);

    UserResult userResult = new UserResult(savedUser.getId(), savedUser.getUsername());

    return new RegisterResult(accessToken, refreshToken, userResult);
  }
}
