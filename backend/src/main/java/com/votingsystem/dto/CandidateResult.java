package com.votingsystem.dto;

/** Built directly by the tally query in VoteRepository. */
public record CandidateResult(Long candidateId, String name, String party, Boolean active, Long votes) { }
