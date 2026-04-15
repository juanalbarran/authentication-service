package com.jarsolutions.authentication_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AuthenticationServiceApplication {

  public static void main(String[] args) {
    System.out.println("Hola Mundo");
    SpringApplication.run(AuthenticationServiceApplication.class, args);
  }
}
