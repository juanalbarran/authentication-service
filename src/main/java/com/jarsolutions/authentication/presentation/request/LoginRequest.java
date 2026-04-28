package com.jarsolutions.authentication.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
    @NotBlank(message = "Username is required to Login")
        @Size(min = 8, max = 50, message = "Username must be between 8 and 50 characters long")
        String username,
    @NotBlank(message = "Password is required to Login")
        @Size(min = 8, max = 50, message = "Password must be between 8 and 50 characters long")
        String password,
    @NotBlank(message = "Device info is required to Login") String deviceInfo) {}
