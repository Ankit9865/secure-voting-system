package com.votingsystem.repository;

import com.votingsystem.dto.CandidateResult;
import com.votingsystem.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface VoteRepository extends JpaRepository<Vote, Long> {

    @Query("""
           select new com.votingsystem.dto.CandidateResult(c.id, c.name, c.party, c.active, count(v))
           from Candidate c left join Vote v on v.candidate = c
           group by c.id, c.name, c.party, c.active
           order by count(v) desc, c.name asc
           """)
    List<CandidateResult> tally();
}
