package com.jarsolutions.authentication.presentation.request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class LoginRequestTest {
  private static Validator validator;

  @BeforeAll
  static void setUp() {
    try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
      validator = factory.getValidator();
    }
  }

  @Test
  void validate_WhenUsernameIsNull_ShoulHaveViolationMessage() {
    LoginRequest request = new LoginRequest(null, "superSecretPassword", "macbook:office");

    Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

    assertEquals(1, violations.size());
    assertEquals("Username is required to Login", violations.iterator().next().getMessage());
  }

  @Test
  void validate_WhenUsernameIsBlank_ShouldHaveViolationMessage() {
    LoginRequest request = new LoginRequest("", "superSecretPassword", "macbook:office");

    Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

    assertEquals(2, violations.size());
    boolean hasBlankMessage =
        violations.stream()
            .anyMatch(violation -> violation.getMessage().equals("Username is required to Login"));
    assertTrue(hasBlankMessage, "Expected the @NotBlank error message, but didnt find it.");
  }

  @Test
  void validate_WhenUsernameIsTooShort_ShouldHaveViolationMessage() {
    LoginRequest request = new LoginRequest("jua", "superSecretPassword", "macbook:office");

    Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

    assertEquals(1, violations.size());
    assertEquals(
        "Username must be between 8 and 50 characters long",
        violations.iterator().next().getMessage());
  }

  @Test
  void validate_WhenPasswordIsNull_ShouldHaveViolationMessage() {
    LoginRequest request = new LoginRequest("juanalbarran", null, "macbook:office");

    Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

    assertEquals(1, violations.size());
    assertEquals("Password is required to Login", violations.iterator().next().getMessage());
  }

  @Test
  void validate_WhenPasswordIsBlank_ShouldHaveViolationMessage() {
    LoginRequest request = new LoginRequest("juanalbarran", "", "macbook:office");

    Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

    assertEquals(2, violations.size());
    boolean hasBlankMessage =
        violations.stream()
            .anyMatch(violation -> violation.getMessage().equals("Password is required to Login"));
    assertTrue(hasBlankMessage, "Expected the @NotBlank error message, but didnt find it.");
  }

  @Test
  void validate_WhenPasswordIsTooShort_ShouldHaveViolationMessage() {
    LoginRequest request = new LoginRequest("juanalbarran", "short", "macbook:office");

    Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

    assertEquals(1, violations.size());
    assertEquals(
        "Password must be between 8 and 50 characters long",
        violations.iterator().next().getMessage());
  }

  @Test
  void validate_WhenDeviceInfoIsNull_ShouldHaveViolationMessage() {
    LoginRequest request = new LoginRequest("juanalbarran", "superSecretPassword", null);

    Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

    assertEquals(1, violations.size());
    assertEquals("Device info is required to Login", violations.iterator().next().getMessage());
  }

  @Test
  void validate_WhenDeviceInfoIsBlank_ShouldHaveViolationMessage() {
    LoginRequest request = new LoginRequest("juanalbarran", "superSecretPassword", "");

    Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

    assertEquals(1, violations.size());
    assertEquals("Device info is required to Login", violations.iterator().next().getMessage());
  }

  @Test
  void validate_WhenAllFieldsAreValid_ShouldHaveNoViolations() {
    LoginRequest request =
        new LoginRequest("juanalbarran", "superSecretPassword", "macbook:office");

    Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

    assertTrue(violations.isEmpty());
  }
}
