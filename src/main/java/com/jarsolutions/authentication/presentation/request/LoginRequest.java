package com.jarsolutions.authentication.presentation.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank(message = "Username is required to Login") String username,
    @NotBlank(message = "Password is required to Login") String password,
    @NotBlank(message = "Device info is required to Login") String deviceInfo) {}
