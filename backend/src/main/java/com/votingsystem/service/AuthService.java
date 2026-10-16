package com.votingsystem.service;

import com.votingsystem.dto.*;
import com.votingsystem.entity.AppUser;
import com.votingsystem.entity.Role;
import com.votingsystem.exception.ApiException;
import com.votingsystem.repository.AppUserRepository;
import com.votingsystem.security.JwtService;
import com.votingsystem.security.LoginAttemptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final AppUserRepository users;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final LoginAttemptService loginAttempts;

    public AuthService(AppUserRepository users, PasswordEncoder encoder, AuthenticationManager authenticationManager,
                       JwtService jwtService, LoginAttemptService loginAttempts) {
        this.users = users;
        this.encoder = encoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.loginAttempts = loginAttempts;
    }

    @Transactional
    public UserResponse register(RegisterRequest req) {
        String email = normalize(req.email());
        if (users.existsByEmail(email)) {
            throw ApiException.conflict("This email is already registered");
        }
        // Role is always USER here; the request has no way to set it.
        AppUser user = new AppUser(req.fullName().trim(), email, encoder.encode(req.password()), Role.USER);
        try {
            users.saveAndFlush(user);
        } catch (DataIntegrityViolationException race) {
            throw ApiException.conflict("This email is already registered");
        }
        log.info("Voter registered, id={}", user.getId());
        return UserResponse.from(user);
    }

    public AuthResponse login(LoginRequest req) {
        String email = normalize(req.email());
        if (loginAttempts.isLocked(email)) {
            throw ApiException.tooManyRequests("Too many failed sign-in attempts. Try again in 15 minutes.");
        }
        try {
            var auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, req.password()));
            loginAttempts.reset(email);
            AppUser user = users.findByEmail(email).orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
            log.info("Login success, userId={}", user.getId());
            return new AuthResponse(jwtService.generateToken((UserDetails) auth.getPrincipal()),
                    "Bearer", jwtService.getExpirationSeconds(), UserResponse.from(user));
        } catch (AuthenticationException ex) {
            loginAttempts.recordFailure(email);
            log.warn("Failed login attempt");
            // Same message whether the email is unknown, the password is wrong, or the account is disabled
            throw new BadCredentialsException("Invalid email or password");
        }
    }

    @Transactional(readOnly = true)
    public UserResponse currentUser(String email) {
        return users.findByEmail(email).map(UserResponse::from)
                .orElseThrow(() -> ApiException.notFound("User not found"));
    }

    private static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
