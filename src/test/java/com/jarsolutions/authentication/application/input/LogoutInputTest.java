package com.jarsolutions.authentication.application.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class LogoutInputTest {

  @Test
  void constructor_WhenValidData_ShouldCreateAnInstance() {
    LogoutInput input = new LogoutInput("fake-token", "macbook");

    assertEquals("fake-token", input.refreshToken());
    assertEquals("macbook", input.deviceInfo());
  }

  @Test
  void constructor_WhenRefreshTokenIsNull_ShouldThrowException() {
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> new LogoutInput(null, "macbook"));
    assertEquals("Refresh token cannot be blank", exception.getMessage());
  }

  @Test
  void constructor_WhenRefreshTokenIsEmpty_SHouldThrowException() {
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> new LogoutInput("", "macbook"));
    assertEquals("Refresh token cannot be blank", exception.getMessage());
  }

  @Test
  void constructor_WhenDeviceInfoIsNull_ShoudThrowException() {
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> new LogoutInput("fake-token", null));
    assertEquals("Device info cannot be blank", exception.getMessage());
  }

  @Test
  void constructor_WhenDeviceInfoIsBlank_ShouldThrowException() {
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> new LogoutInput("fake-token", ""));
    assertEquals("Device info cannot be blank", exception.getMessage());
  }
}
