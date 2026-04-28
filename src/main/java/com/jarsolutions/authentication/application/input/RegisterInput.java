package com.jarsolutions.authentication.application.input;

import static com.jarsolutions.authentication.application.util.ValidationUtil.minimumSize;
import static com.jarsolutions.authentication.application.util.ValidationUtil.requireNonBlank;

public record RegisterInput(String username, String password, String deviceInfo) {
  public RegisterInput {
    requireNonBlank(username, "Username");
    requireNonBlank(password, "Password");
    requireNonBlank(deviceInfo, "Device Info");
    minimumSize(username, 8, "Username");
    minimumSize(password, 8, "Password");
  }
}
