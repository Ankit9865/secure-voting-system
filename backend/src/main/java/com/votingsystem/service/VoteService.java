package com.votingsystem.service;

import com.votingsystem.entity.AppUser;
import com.votingsystem.entity.Candidate;
import com.votingsystem.entity.Vote;
import com.votingsystem.exception.ApiException;
import com.votingsystem.repository.AppUserRepository;
import com.votingsystem.repository.CandidateRepository;
import com.votingsystem.repository.VoteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VoteService {

    private static final Logger log = LoggerFactory.getLogger(VoteService.class);

    private final AppUserRepository users;
    private final CandidateRepository candidates;
    private final VoteRepository votes;

    public VoteService(AppUserRepository users, CandidateRepository candidates, VoteRepository votes) {
        this.users = users;
        this.candidates = candidates;
        this.votes = votes;
    }

    /**
     * One transaction does two things: claim the voter's single vote, and store the anonymous ballot.
     * The claim is a conditional UPDATE (has_voted = false -> true), so two simultaneous requests from
     * the same user cannot both succeed. If anything fails, the whole transaction rolls back and the
     * user can try again. We log THAT a vote happened, never WHICH candidate, to keep the ballot secret.
     */
    @Transactional
    public void castVote(String email, Long candidateId) {
        AppUser user = users.findByEmail(email).orElseThrow(() -> ApiException.notFound("User not found"));
        Candidate candidate = candidates.findById(candidateId)
                .filter(Candidate::isActive)
                .orElseThrow(() -> ApiException.notFound("Candidate not found"));

        if (users.markVoted(user.getId()) == 0) {
            throw ApiException.conflict("You have already voted");
        }
        votes.save(new Vote(candidate));
        log.info("Vote recorded for userId={}", user.getId());
    }

    @Transactional(readOnly = true)
    public boolean hasVoted(String email) {
        return users.findByEmail(email).map(AppUser::hasVoted)
                .orElseThrow(() -> ApiException.notFound("User not found"));
    }
}
