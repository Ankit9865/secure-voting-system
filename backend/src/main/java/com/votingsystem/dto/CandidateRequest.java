package com.votingsystem.dto;

import jakarta.validation.constraints.*;

public record CandidateRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 100) String party) { }
