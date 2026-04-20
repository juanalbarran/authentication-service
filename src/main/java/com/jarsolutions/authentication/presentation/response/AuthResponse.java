package com.jarsolutions.authentication.presentation.response;

import lombok.Data;

@Data
public class AuthResponse {

  public static final String TOKEN_TYPE = "Bearer";

  private String accessToken;

  private Long id;
  private String username;

  public AuthResponse(String accessToken, Long id, String username) {
    this.id = id;
    this.username = username;
    this.accessToken = accessToken;
  }
}
