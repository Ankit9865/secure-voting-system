package com.votingsystem.service;

import com.votingsystem.dto.CandidateRequest;
import com.votingsystem.dto.CandidateResponse;
import com.votingsystem.entity.Candidate;
import com.votingsystem.exception.ApiException;
import com.votingsystem.repository.CandidateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CandidateService {

    private final CandidateRepository candidates;

    public CandidateService(CandidateRepository candidates) {
        this.candidates = candidates;
    }

    @Transactional(readOnly = true)
    public List<CandidateResponse> listActive() {
        return candidates.findByActiveTrueOrderByNameAsc().stream().map(CandidateResponse::from).toList();
    }

    @Transactional
    public CandidateResponse create(CandidateRequest req) {
        String name = req.name().trim();
        if (candidates.existsByNameIgnoreCase(name)) {
            throw ApiException.conflict("A candidate with this name already exists");
        }
        return CandidateResponse.from(candidates.save(new Candidate(name, req.party().trim())));
    }

    @Transactional
    public CandidateResponse update(Long id, CandidateRequest req) {
        Candidate c = find(id);
        String name = req.name().trim();
        if (!c.getName().equalsIgnoreCase(name) && candidates.existsByNameIgnoreCase(name)) {
            throw ApiException.conflict("A candidate with this name already exists");
        }
        c.setName(name);
        c.setParty(req.party().trim());
        return CandidateResponse.from(c);
    }

    /** Soft delete: past ballots reference the candidate, so the row must stay for the results. */
    @Transactional
    public void deactivate(Long id) {
        find(id).setActive(false);
    }

    private Candidate find(Long id) {
        return candidates.findById(id).orElseThrow(() -> ApiException.notFound("Candidate not found"));
    }
}
