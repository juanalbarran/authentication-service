package com.jarsolutions.authentication.presentation.response;

import java.time.Instant;

public record ErrorResponse(int status, String error, String message, Instant timestamp) {}
