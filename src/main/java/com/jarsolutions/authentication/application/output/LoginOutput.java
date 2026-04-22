package com.jarsolutions.authentication.application.output;

public record LoginOutput(String accessToken, String refreshToken, UserOutput userOutput) {}
