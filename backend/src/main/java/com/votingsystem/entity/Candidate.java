package com.votingsystem.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "candidates")
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String party;

    @Column(nullable = false)
    private boolean active = true;

    protected Candidate() { }

    public Candidate(String name, String party) {
        this.name = name;
        this.party = party;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getParty() { return party; }
    public boolean isActive() { return active; }

    public void setName(String name) { this.name = name; }
    public void setParty(String party) { this.party = party; }
    public void setActive(boolean active) { this.active = active; }
}
