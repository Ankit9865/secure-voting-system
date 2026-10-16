package com.votingsystem.entity;

import jakarta.persistence.*;
import java.time.Instant;

/** An anonymous ballot: it records WHAT was chosen, never WHO chose it. */
@Entity
@Table(name = "votes")
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @Column(nullable = false, updatable = false)
    private Instant castAt = Instant.now();

    protected Vote() { }

    public Vote(Candidate candidate) {
        this.candidate = candidate;
    }

    public Long getId() { return id; }
    public Candidate getCandidate() { return candidate; }
    public Instant getCastAt() { return castAt; }
}
