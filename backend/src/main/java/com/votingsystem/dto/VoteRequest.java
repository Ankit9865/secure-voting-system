package com.votingsystem.dto;

import jakarta.validation.constraints.NotNull;

public record VoteRequest(@NotNull Long candidateId) { }
