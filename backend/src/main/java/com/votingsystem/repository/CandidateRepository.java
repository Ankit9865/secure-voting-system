package com.votingsystem.repository;

import com.votingsystem.entity.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    boolean existsByNameIgnoreCase(String name);

    List<Candidate> findByActiveTrueOrderByNameAsc();
}
