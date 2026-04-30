package com.jarsolutions.authentication.application.input;

import static com.jarsolutions.authentication.application.util.ValidationUtil.requireNonBlank;

public record LogoutInput(String refreshToken, String deviceInfo) {
  public LogoutInput {
    requireNonBlank(refreshToken, "Refresh token");
    requireNonBlank(deviceInfo, "Device info");
  }
}
