package com.votingsystem.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Optional;

/** Creates and verifies signed (HS256) access tokens. Tokens expire; the secret never lives in code. */
@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(@Value("${app.jwt.secret}") String base64Secret,
                      @Value("${app.jwt.expiration-ms}") long expirationMs) {
        byte[] bytes = Decoders.BASE64.decode(base64Secret);
        if (bytes.length < 32) {
            // Fail at startup instead of running with a weak key
            throw new IllegalStateException("JWT_SECRET must be a Base64 string of at least 32 bytes (openssl rand -base64 32)");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
        this.expirationMs = expirationMs;
    }

    public String generateToken(UserDetails user) {
        Date now = new Date();
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("roles", user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(key)
                .compact();
    }

    /** Returns the username only if the signature is valid and the token has not expired. */
    public Optional<String> extractUsername(String token) {
        try {
            return Optional.ofNullable(
                    Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject());
        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    public long getExpirationSeconds() { return expirationMs / 1000; }
}
