package com.jarsolutions.authentication.presentation.request;

public record LoginRequest(String username, String password, String deviceInfo) {}
