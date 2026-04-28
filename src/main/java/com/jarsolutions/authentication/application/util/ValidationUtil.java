package com.jarsolutions.authentication.application.util;

public final class ValidationUtil {
  private ValidationUtil() {}

  public static void requireNonBlank(String field, String fieldName) {
    if (field == null || field.isBlank()) {
      throw new IllegalArgumentException(fieldName + " cannot be blank");
    }
  }

  public static void minimumSize(String field, int size, String fieldName) {
    if (field.length() < size) {
      throw new IllegalArgumentException(
          fieldName + " should have more than " + size + " characters");
    }
  }
}
