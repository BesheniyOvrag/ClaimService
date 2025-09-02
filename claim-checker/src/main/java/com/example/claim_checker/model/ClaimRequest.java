package com.example.claim_checker.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClaimRequest {
    private String policyType;
    private String name;
    private String surname;
    private String email;
    private String date;
    private String description;
}

