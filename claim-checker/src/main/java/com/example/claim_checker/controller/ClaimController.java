package com.example.claim_checker.controller;

import com.example.claim_checker.entity.Claim;
import com.example.claim_checker.model.ClaimRequest;
import com.example.claim_checker.model.ClaimResponse;
import com.example.claim_checker.service.ClaimCheckerService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/claims")
@RequiredArgsConstructor
public class ClaimController {

    private final ClaimCheckerService claimCheckerService;

    @PostMapping
    public ClaimResponse checkClaim(@RequestBody ClaimRequest request) throws JsonProcessingException {
        return claimCheckerService.checkClaim(request);
    }

    @GetMapping
    public List<Claim> getClaims() {
        return claimCheckerService.getAllClaims();
    }

}
