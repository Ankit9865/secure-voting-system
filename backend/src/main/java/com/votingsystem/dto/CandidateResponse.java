package com.votingsystem.dto;

import com.votingsystem.entity.Candidate;

public record CandidateResponse(Long id, String name, String party) {
    public static CandidateResponse from(Candidate c) {
        return new CandidateResponse(c.getId(), c.getName(), c.getParty());
    }
}
