package com.votingsystem.security;

import com.votingsystem.entity.AppUser;
import com.votingsystem.repository.AppUserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AppUserRepository users;

    public CustomUserDetailsService(AppUserRepository users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AppUser u = users.findByEmail(email.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return User.withUsername(u.getEmail())
                .password(u.getPasswordHash())
                .roles(u.getRole().name())          // becomes ROLE_USER / ROLE_ADMIN
                .disabled(!u.isEnabled())
                .build();
    }
}
