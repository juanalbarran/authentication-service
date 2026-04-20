package com.jarsolutions.authentication.application;

import com.jarsolutions.authentication.domain.User;
import com.jarsolutions.authentication.domain.UserRepository;
import lombok.Data;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Data
@Service
public class AuthenticationService {

  private final UserRepository userRepository;
  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;

  public AuthenticationService(
      UserRepository userRepository,
      AuthenticationManager authenticationManager,
      JwtService jwtService) {
    this.userRepository = userRepository;
    this.authenticationManager = authenticationManager;
    this.jwtService = jwtService;
  }

  public record LoginResult(String accessToken, String RefreshToken, User user) {}
  ;

  public LoginResult login(String username, String password, String deviceType) {
    authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

    return null;
  }
}
