package com.jarsolutions.authentication.application.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class RegisterInputTest {

  @Test
  void constructor_WhenValidData_ShouldCreateInstance() {
    RegisterInput input = new RegisterInput("juan.albarran", "passwordcool", "macbook:office");

    assertEquals("juan.albarran", input.username());
    assertEquals("passwordcool", input.password());
    assertEquals("macbook:office", input.deviceInfo());
  }

  @Test
  void constructor_WhenUsernameIsBlank_ShouldThrowException() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> new RegisterInput("", "superSecretPassword", "macbook:office"));
    assertEquals("Username cannot be blank", exception.getMessage());
  }

  @Test
  void constructor_WhenUsernameIsNull_ShouldThrowException() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> new RegisterInput(null, "superSecretPassword", "macbook:office"));
    assertEquals("Username cannot be blank", exception.getMessage());
  }

  @Test
  void constructor_WhenUsernameIsShort_ShouldThrowException() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> new RegisterInput("juan", "superSecretPassword", "macbook:office"));
    assertEquals("Username should have more than 8 characters", exception.getMessage());
  }

  @Test
  void constructor_WhenPasswordIsBlank_ShouldThrowException() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> new RegisterInput("juan.albarran", "", "macbook:office"));
    assertEquals("Password cannot be blank", exception.getMessage());
  }

  @Test
  void constructor_WhenPasswordIsNull_ShouldThrowException() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> new RegisterInput("juan.albarran", null, "macbook:office"));
    assertEquals("Password cannot be blank", exception.getMessage());
  }

  @Test
  void constructor_WhenPasswordIsTooShort_ShouldThrowException() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> new RegisterInput("juan.albarran", "hey", "macbook:office"));
    assertEquals("Password should have more than 8 characters", exception.getMessage());
  }

  @Test
  void constructor_WhenDeviceInfoIsNull_ShouldThrowException() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> new LoginInput("juan.albarran", "superSecretPassword", null));
    assertEquals("Device Info cannot be blank", exception.getMessage());
  }

  @Test
  void constructor_WhenDeviceInfoIsBlank_ShouldThrowException() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> new RegisterInput("juan.albarran", "superSecretPassword", ""));
    assertEquals("Device Info cannot be blank", exception.getMessage());
  }
}
