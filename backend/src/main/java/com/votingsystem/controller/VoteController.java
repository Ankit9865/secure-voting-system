package com.votingsystem.controller;

import com.votingsystem.dto.MessageResponse;
import com.votingsystem.dto.VoteRequest;
import com.votingsystem.dto.VoteStatus;
import com.votingsystem.service.VoteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/votes")
public class VoteController {

    private final VoteService voteService;

    public VoteController(VoteService voteService) {
        this.voteService = voteService;
    }

    @PostMapping
    public ResponseEntity<MessageResponse> vote(@Valid @RequestBody VoteRequest request,
                                                @AuthenticationPrincipal UserDetails principal) {
        voteService.castVote(principal.getUsername(), request.candidateId());
        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("Your vote has been recorded"));
    }

    @GetMapping("/status")
    public VoteStatus status(@AuthenticationPrincipal UserDetails principal) {
        return new VoteStatus(voteService.hasVoted(principal.getUsername()));
    }
}
