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

public class RegisterRequestTest {
  private static Validator validator;

  @BeforeAll
  static void setUp() {
    try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
      validator = factory.getValidator();
    }
  }

  @Test
  void validate_WhenUsernameIsNull_ShoulHaveViolationMessage() {
    RegisterRequest request = new RegisterRequest(null, "superSecretPassword", "macbook:office");

    Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

    assertEquals(1, violations.size());
    assertEquals("Username cannot be blank", violations.iterator().next().getMessage());
  }

  @Test
  void validate_WhenUsernameIsBlank_ShouldHaveViolationMessage() {
    RegisterRequest request = new RegisterRequest("", "superSecretPassword", "macbook:office");

    Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

    assertEquals(2, violations.size());
    boolean hasBlankMessage =
        violations.stream()
            .anyMatch(violation -> violation.getMessage().equals("Username cannot be blank"));
    assertTrue(hasBlankMessage, "Expected the @NotBlank error message, but didnt find it.");
  }

  @Test
  void validate_WhenUsernameIsTooShort_ShouldHaveViolationMessage() {
    RegisterRequest request = new RegisterRequest("jua", "superSecretPassword", "macbook:office");

    Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

    assertEquals(1, violations.size());
    assertEquals(
        "Username must be between 8 and 50 characters long",
        violations.iterator().next().getMessage());
  }

  @Test
  void validate_WhenPasswordIsNull_ShouldHaveViolationMessage() {
    RegisterRequest request = new RegisterRequest("juanalbarran", null, "macbook:office");

    Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

    assertEquals(1, violations.size());
    assertEquals("Password cannot be blank", violations.iterator().next().getMessage());
  }

  @Test
  void validate_WhenPasswordIsBlank_ShouldHaveViolationMessage() {
    RegisterRequest request = new RegisterRequest("juanalbarran", "", "macbook:office");

    Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

    assertEquals(2, violations.size());
    boolean hasBlankMessage =
        violations.stream()
            .anyMatch(violation -> violation.getMessage().equals("Password cannot be blank"));
    assertTrue(hasBlankMessage, "Expected the @NotBlank error message, but didnt find it.");
  }

  @Test
  void validate_WhenPasswordIsTooShort_ShouldHaveViolationMessage() {
    RegisterRequest request = new RegisterRequest("juanalbarran", "short", "macbook:office");

    Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

    assertEquals(1, violations.size());
    assertEquals(
        "Password must be at least 8 characters long", violations.iterator().next().getMessage());
  }

  @Test
  void validate_WhenDeviceInfoIsNull_ShouldHaveViolationMessage() {
    RegisterRequest request = new RegisterRequest("juanalbarran", "superSecretPassword", null);

    Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

    assertEquals(1, violations.size());
    assertEquals(
        "Device info is required to secure your session",
        violations.iterator().next().getMessage());
  }

  @Test
  void validate_WhenDeviceInfoIsBlank_ShouldHaveViolationMessage() {
    RegisterRequest request = new RegisterRequest("juanalbarran", "superSecretPassword", "");

    Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

    assertEquals(1, violations.size());
    assertEquals(
        "Device info is required to secure your session",
        violations.iterator().next().getMessage());
  }

  @Test
  void validate_WhenAllFieldsAreValid_ShouldHaveNoViolations() {
    RegisterRequest request =
        new RegisterRequest("juanalbarran", "superSecretPassword", "macbook:office");

    Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

    assertTrue(violations.isEmpty());
  }
}
