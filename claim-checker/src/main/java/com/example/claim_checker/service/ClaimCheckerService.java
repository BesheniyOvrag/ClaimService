package com.example.claim_checker.service;

import com.example.claim_checker.entity.Claim;

import com.example.claim_checker.model.ClaimRequest;
import com.example.claim_checker.model.ClaimResponse;
import com.example.claim_checker.model.PolicyType;
import com.example.claim_checker.repository.ClaimRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDate;


@Service
@RequiredArgsConstructor
public class ClaimCheckerService {

    private final ClaimRepository claimRepository;
    private final AiService aiService;

    public ClaimResponse checkClaim(ClaimRequest request) {


        String decision = aiService.classifyClaim(request);

        decision = decision.replace("*", "");

        Claim claim = Claim.builder()
                .policyType(PolicyType.valueOf(request.getPolicyType().toUpperCase()))
                .name(request.getName())
                .surname(request.getSurname())
                .email(request.getEmail())
                .claimDate(LocalDate.parse(request.getDate()))
                .description(request.getDescription())
                .decision(decision)
                .booleanDecision(decision.contains("Likely to approve"))
                .build();


        claimRepository.save(claim);

        return new ClaimResponse(decision);
    }

    public java.util.List<Claim> getAllClaims() {
        return claimRepository.findAll();
    }
}
