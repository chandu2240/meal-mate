package com.mealmate.gateway.security;

import java.time.Instant;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
public class TokenController {
    private final JwtEncoder encoder;

    public TokenController(JwtEncoder encoder) { this.encoder = encoder; }

    @PostMapping("/token")
    public Map<String, Object> token(@RequestBody(required = false) Credentials credentials) {
        if (credentials == null || !"demo".equals(credentials.username()) || !"demo".equals(credentials.password())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Use demo/demo for the assignment demonstration");
        }
        var now = Instant.now();
        var claims = JwtClaimsSet.builder().issuer("meal-mate-gateway").subject(credentials.username())
                .issuedAt(now).expiresAt(now.plusSeconds(3600)).claim("scope", "meal-mate").build();
        var value = encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        return Map.of("token", value, "access_token", value, "accessToken", value,
                "token_type", "Bearer", "expires_in", 3600);
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody(required = false) Credentials credentials) {
        return token(credentials);
    }

    public record Credentials(String username, String password) {}
}
