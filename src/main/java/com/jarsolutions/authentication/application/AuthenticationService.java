package com.jarsolutions.authentication.application;

import com.jarsolutions.authentication.application.input.LoginInput;
import com.jarsolutions.authentication.application.input.RefreshInput;
import com.jarsolutions.authentication.application.input.RegisterInput;
import com.jarsolutions.authentication.application.output.LoginOutput;
import com.jarsolutions.authentication.application.output.RegisterOutput;
import com.jarsolutions.authentication.application.output.TokenOutput;
import com.jarsolutions.authentication.application.output.UserOutput;
import com.jarsolutions.authentication.domain.entity.User;
import com.jarsolutions.authentication.domain.entity.UserSession;
import com.jarsolutions.authentication.domain.exception.UserAlreadyExistsException;
import com.jarsolutions.authentication.domain.repository.UserRepository;
import com.jarsolutions.authentication.domain.repository.UserSessionRepository;
import jakarta.transaction.Transactional;
import java.time.Instant;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class AuthenticationService {

  private final UserRepository userRepository;
  private final UserSessionRepository userSessionRepository;
  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;
  private final PasswordEncoder passwordEncoder;

  private record CreateUserInput(String username, String password) {}

  private record CreateUserSessionInput(String refreshToken, String deviceInfo, User user) {}

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

  public LoginOutput login(LoginInput loginInput) {
    String username = loginInput.username();
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(username, loginInput.password()));

    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(
                () -> new UsernameNotFoundException("There is no user with that username."));

    TokenOutput tokens = createTokens(username);

    createUserSession(
        new CreateUserSessionInput(tokens.refreshToken(), loginInput.deviceInfo(), user));

    UserOutput userOutput = new UserOutput(user.getId(), username);

    return new LoginOutput(tokens, userOutput);
  }

  public RegisterOutput register(RegisterInput registerInput) throws UserAlreadyExistsException {

    String username = registerInput.username();
    String password = registerInput.password();

    if (userRepository.existsByUsername(username)) {
      throw new UserAlreadyExistsException("User " + username + " already exists.");
    }

    User user = createUser(new CreateUserInput(username, password));
    TokenOutput tokens = createTokens(username);

    createUserSession(
        new CreateUserSessionInput(tokens.refreshToken(), registerInput.deviceInfo(), user));
    UserOutput userOutput = new UserOutput(user.getId(), username);

    return new RegisterOutput(tokens, userOutput);
  }

  public TokenOutput refresh(RefreshInput refreshInput) {
    String oldToken = refreshInput.refreshToken();

    UserSession session =
        userSessionRepository
            .findByToken(oldToken)
            .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

    if (session.getExpiryDate().isBefore(Instant.now())) {
      userSessionRepository.delete(session);
      throw new IllegalArgumentException("Refresh token has expired. Please log in again.");
    }

    User user = session.getUser();
    String username = user.getUsername();

    TokenOutput newTokens = createTokens(username);

    userSessionRepository.delete(session);

    createUserSession(
        new CreateUserSessionInput(newTokens.refreshToken(), refreshInput.deviceInfo(), user));
    return newTokens;
  }

  private User createUser(CreateUserInput createUserInput) {
    String hashedPassword = passwordEncoder.encode(createUserInput.password());
    User user = new User(createUserInput.username(), hashedPassword);
    User savedUser = userRepository.save(user);
    return savedUser;
  }

  private TokenOutput createTokens(String username) {
    String accessToken = jwtService.generateAccessToken(username);
    String refreshToken = jwtService.generateRefreshToken(username);
    return new TokenOutput(accessToken, refreshToken);
  }

  private void createUserSession(CreateUserSessionInput createUserSessionInput) {
    Instant refreshTokenExpiration =
        Instant.now().plusMillis(jwtService.getRefreshTokenExpiration());
    UserSession userSession =
        new UserSession(
            createUserSessionInput.refreshToken(),
            createUserSessionInput.deviceInfo(),
            refreshTokenExpiration,
            createUserSessionInput.user());
    userSessionRepository.save(userSession);
  }
}
