package com.jarsolutions.authentication.application;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  public JwtService() {}

  @Value("${application.security.jwt.secret-key:Yn2kjibddFAWtnPJ2AFlL8WXpouVwuNode2n17382cI=}")
  private String secretKey;

  @Value("${application.security.jwt.access-token-expiration:900000}")
  private long accessTokenExpiration;

  @Value("${application.security.jwt.refresh-token-expiration:604800000}")
  private long refreshTokenExpiration;

  public String generateAccessToken(String username) {
    return buildToken(username, accessTokenExpiration);
  }

  public String generateRefreshToken(String username) {
    return buildToken(username, refreshTokenExpiration);
  }

  public long getRefreshTokenExpiration() {
    return refreshTokenExpiration;
  }

  private String buildToken(String username, long expirationTime) {
    return Jwts.builder()
        .subject(username)
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + expirationTime))
        .signWith(getSignInKey())
        .compact();
  }

  private SecretKey getSignInKey() {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    return Keys.hmacShaKeyFor(keyBytes);
  }
}
