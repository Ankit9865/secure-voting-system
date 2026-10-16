package com.votingsystem.dto;

import jakarta.validation.constraints.*;

public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(max = 64) String password) { }
