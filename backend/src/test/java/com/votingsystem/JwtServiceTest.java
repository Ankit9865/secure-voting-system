package com.votingsystem;

import com.votingsystem.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "U3xQo3neqD/oGlwz4RV6HCVWbo7RJDN71tRgeQHXlgs=";
    private final UserDetails user = User.withUsername("a@example.com").password("x").roles("USER").build();

    @Test
    void validTokenReturnsTheUsername() {
        JwtService jwt = new JwtService(SECRET, 60_000);
        assertThat(jwt.extractUsername(jwt.generateToken(user))).contains("a@example.com");
    }

    @Test
    void expiredTokenIsRejected() {
        JwtService jwt = new JwtService(SECRET, -1_000);
        assertThat(jwt.extractUsername(jwt.generateToken(user))).isEmpty();
    }

    @Test
    void tokenWithForgedSignatureIsRejected() {
        JwtService jwt = new JwtService(SECRET, 60_000);
        String token = jwt.generateToken(user);
        String forged = token.substring(0, token.lastIndexOf('.') + 1) + "AAAA";
        assertThat(jwt.extractUsername(forged)).isEmpty();
    }

    @Test
    void garbageIsRejected() {
        JwtService jwt = new JwtService(SECRET, 60_000);
        assertThat(jwt.extractUsername("not.a.token")).isEmpty();
    }

    @Test
    void weakSecretIsRejectedAtStartup() {
        assertThatThrownBy(() -> new JwtService("c2hvcnQ=", 1_000)).isInstanceOf(IllegalStateException.class);
    }
}
