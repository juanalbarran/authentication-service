package com.jarsolutions.authentication.application.input;

import static com.jarsolutions.authentication.application.util.ValidationUtil.requireNonBlank;

public record RefreshInput(String refreshToken, String deviceInfo) {
  public RefreshInput {
    requireNonBlank(refreshToken, "Refresh Token");
    requireNonBlank(deviceInfo, "Device info");
  }
}
