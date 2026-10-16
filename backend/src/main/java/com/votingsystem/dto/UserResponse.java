package com.votingsystem.dto;

import com.votingsystem.entity.AppUser;

public record UserResponse(Long id, String fullName, String email, String role, boolean hasVoted) {
    public static UserResponse from(AppUser u) {
        return new UserResponse(u.getId(), u.getFullName(), u.getEmail(), u.getRole().name(), u.hasVoted());
    }
}
