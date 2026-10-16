package com.votingsystem.dto;

public record ElectionStats(long registeredVoters, long votesCast, double turnoutPercent) { }
