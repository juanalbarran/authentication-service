package com.jarsolutions.authentication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AuthenticationApplication {

  public static void main(String[] args) {
    System.out.println("Hola Mundo");
    SpringApplication.run(AuthenticationApplication.class, args);
  }
}
