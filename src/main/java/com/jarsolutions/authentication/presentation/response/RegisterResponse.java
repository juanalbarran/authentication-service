package com.jarsolutions.authentication.presentation.response;

import lombok.Data;

@Data
public class RegisterResponse {

  private String accessToken;

  public RegisterResponse(String accessToken) {
    this.accessToken = accessToken;
  }
}
