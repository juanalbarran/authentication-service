# authentication-service

Just some JWT Authentication Service using Java and Spring Boot

## Security

### Cross-Site Request Forgery (CSRF)

The `ResponseCookie` contains the `.sameSite("Strict")` attribute to tell the browser, "Send this cookie only if the request came from our official fronten domain"

```Java
ResponseCookie cookie = ResponseCookie.from("refresh_token", tokens.refreshToken())
        .httpOnly(true)
        .secure(true)
        .sameSite("Strict")
        .path("/")
```

### Cross-Orign Resource Sharing (CORS)

For this we limit the frontend URLs that can access the `authentication-service` to the ones we have whitelisted.
The `SecurityConfig` class contain the configuration

### TODO

Prepare defenses against brute force attacks

- Account Lockdown
- Rate Limiting
