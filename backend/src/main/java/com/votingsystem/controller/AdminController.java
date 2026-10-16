package com.votingsystem.controller;

import com.votingsystem.dto.*;
import com.votingsystem.service.CandidateService;
import com.votingsystem.service.ResultService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Everything under /api/admin/** requires ROLE_ADMIN (enforced in SecurityConfig). */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final CandidateService candidateService;
    private final ResultService resultService;

    public AdminController(CandidateService candidateService, ResultService resultService) {
        this.candidateService = candidateService;
        this.resultService = resultService;
    }

    @PostMapping("/candidates")
    public ResponseEntity<CandidateResponse> create(@Valid @RequestBody CandidateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(candidateService.create(request));
    }

    @PutMapping("/candidates/{id}")
    public CandidateResponse update(@PathVariable Long id, @Valid @RequestBody CandidateRequest request) {
        return candidateService.update(id, request);
    }

    @DeleteMapping("/candidates/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        candidateService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/results")
    public List<CandidateResult> results() {
        return resultService.results();
    }

    @GetMapping("/stats")
    public ElectionStats stats() {
        return resultService.stats();
    }
}
