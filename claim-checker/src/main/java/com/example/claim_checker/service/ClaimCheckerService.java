package com.example.claim_checker.service;

import com.example.claim_checker.entity.Claim;

import com.example.claim_checker.model.ClaimRequest;
import com.example.claim_checker.model.ClaimResponse;
import com.example.claim_checker.repository.ClaimRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class ClaimCheckerService {

    private final ClaimRepository claimRepository;
    private final AiService aiService;

    public ClaimResponse checkClaim(ClaimRequest request) {


        String decision = aiService.classifyClaim(request);

        Claim claim = Claim.builder()
                .policyType(request.getPolicyType())
                .date(request.getDate())
                .description(request.getDescription())
                .decision(decision)
                .build();
        claimRepository.save(claim);

        return new ClaimResponse(decision);
    }

    public java.util.List<Claim> getAllClaims() {
        return claimRepository.findAll();
    }
}
