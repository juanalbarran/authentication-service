package com.jarsolutions.authentication.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.jsonwebtoken.security.WeakKeyException;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

public class JwtServiceTest {
  private JwtService jwtService;

  @BeforeEach
  void setUp() {
    jwtService = new JwtService();

    ReflectionTestUtils.setField(
        jwtService, "secretKey", "Yn2kjibddFAWtnPJ2AFlL8WXpouVwuNode2n17382cI=");
    ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", 900000L);
    ReflectionTestUtils.setField(jwtService, "refreshTokenExpiration", 604800000L);
  }

  @Test
  void generateAccessToken_ShouldReturnValidTokenString() {
    verifyTokenGeneration(jwtService::generateAccessToken);
  }

  @Test
  void generateRefreshToken_ShouldReturnValidTokenString() {
    verifyTokenGeneration(jwtService::generateRefreshToken);
  }

  @Test
  void generateAccessToken_WhenSecretKeyIsWeak_ShoudlThrowWeakKeyException() {
    JwtService weakJwtService = new JwtService();

    ReflectionTestUtils.setField(weakJwtService, "secretKey", "secreto=papi");
    ReflectionTestUtils.setField(weakJwtService, "accessTokenExpiration", 900000L);

    assertThrows(
        WeakKeyException.class,
        () -> {
          weakJwtService.generateAccessToken("juan.albarran");
        });
  }

  // TODO: Make a test that check that the secret-key does not have any illegal characters

  private void verifyTokenGeneration(Function<String, String> tokenGeneration) {
    String username = "juan.albarran";
    String token = tokenGeneration.apply(username);

    assertNotNull(token);
    assertFalse(token.isBlank());

    assertEquals(3, token.split("\\.").length);
  }
}
