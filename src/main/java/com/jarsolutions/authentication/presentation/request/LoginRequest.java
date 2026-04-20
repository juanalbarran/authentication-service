package com.jarsolutions.authentication.presentation.request;

import lombok.Data;

@Data
public class LoginRequest {

  private String username;
  private String password;
  private String deviceType;

  public LoginRequest(String username, String password, String deviceType) {
    this.username = username;
    this.password = password;
    this.deviceType = deviceType;
  }
}
