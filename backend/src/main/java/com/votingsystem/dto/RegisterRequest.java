package com.votingsystem.dto;

import jakarta.validation.constraints.*;

/** Note: there is deliberately no "role" field, so clients can never choose their own role. */
public record RegisterRequest(
        @NotBlank @Size(max = 100) String fullName,
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,64}$",
                 message = "Use 8-64 characters with an uppercase letter, a lowercase letter, a number and a symbol")
        String password) { }
