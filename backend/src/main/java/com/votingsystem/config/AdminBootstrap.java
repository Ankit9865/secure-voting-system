package com.votingsystem.config;

import com.votingsystem.entity.AppUser;
import com.votingsystem.entity.Role;
import com.votingsystem.repository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Locale;

/** Creates the first admin from ADMIN_EMAIL / ADMIN_PASSWORD. No default credentials exist in the code. */
@Component
public class AdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final AppUserRepository users;
    private final PasswordEncoder encoder;
    private final String email;
    private final String password;

    public AdminBootstrap(AppUserRepository users, PasswordEncoder encoder,
                          @Value("${app.bootstrap-admin.email}") String email,
                          @Value("${app.bootstrap-admin.password}") String password) {
        this.users = users;
        this.encoder = encoder;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (email.isBlank() || password.isBlank()) {
            log.info("No bootstrap admin configured (set ADMIN_EMAIL and ADMIN_PASSWORD to create one)");
            return;
        }
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        if (users.existsByEmail(normalized)) return;
        if (password.length() < 12) {
            throw new IllegalStateException("ADMIN_PASSWORD must be at least 12 characters");
        }
        users.save(new AppUser("Administrator", normalized, encoder.encode(password), Role.ADMIN));
        log.info("Bootstrap admin account created");
    }
}
