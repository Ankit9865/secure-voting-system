package com.votingsystem.service;

import com.votingsystem.dto.CandidateResult;
import com.votingsystem.dto.ElectionStats;
import com.votingsystem.entity.Role;
import com.votingsystem.repository.AppUserRepository;
import com.votingsystem.repository.VoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ResultService {

    private final AppUserRepository users;
    private final VoteRepository votes;

    public ResultService(AppUserRepository users, VoteRepository votes) {
        this.users = users;
        this.votes = votes;
    }

    @Transactional(readOnly = true)
    public List<CandidateResult> results() {
        return votes.tally();
    }

    @Transactional(readOnly = true)
    public ElectionStats stats() {
        long registered = users.countByRole(Role.USER);
        long cast = votes.count();
        double turnout = registered == 0 ? 0 : Math.round(cast * 1000.0 / registered) / 10.0;
        return new ElectionStats(registered, cast, turnout);
    }
}
