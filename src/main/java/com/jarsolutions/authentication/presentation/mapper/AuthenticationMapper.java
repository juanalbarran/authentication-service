package com.jarsolutions.authentication.presentation.mapper;

import com.jarsolutions.authentication.application.input.LoginInput;
import com.jarsolutions.authentication.application.input.RegisterInput;
import com.jarsolutions.authentication.application.output.LoginOutput;
import com.jarsolutions.authentication.application.output.RegisterOutput;
import com.jarsolutions.authentication.presentation.request.LoginRequest;
import com.jarsolutions.authentication.presentation.request.RegisterRequest;
import com.jarsolutions.authentication.presentation.response.LoginResponse;
import com.jarsolutions.authentication.presentation.response.RegisterResponse;

public class AuthenticationMapper {

  private AuthenticationMapper() {}

  public static LoginInput toLoginInput(LoginRequest loginRequest) {
    return new LoginInput(
        loginRequest.username(), loginRequest.password(), loginRequest.deviceInfo());
  }

  public static LoginResponse toLoginResponse(LoginOutput loginOutput) {
    return new LoginResponse(loginOutput.userOutput().id(), loginOutput.userOutput().username());
  }

  public static RegisterInput toRegisterInput(RegisterRequest registerRequest) {
    return new RegisterInput(
        registerRequest.username(), registerRequest.password(), registerRequest.deviceInfo());
  }

  public static RegisterResponse toRegisterResponse(RegisterOutput registerOutput) {
    return new RegisterResponse(registerOutput.tokenOutput().accessToken());
  }
}
