package com.example.claim_checker.entity;

import com.example.claim_checker.model.PolicyType;
import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "claims")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Claim {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "policy_type", nullable = false, length = 20)
    private PolicyType policyType;

    @Column(length = 50, nullable = false)
    private String name;

    @Column(length = 50, nullable = false)
    private String surname;

    @Column(length = 50, nullable = false)
    private String email;

    @Column(name = "claim_date", nullable = false)
    private java.time.LocalDate claimDate;

    @Column(length = 2000, nullable = false)
    private String description;

    @Column(length = 2000)
    private String decision;

    @Column(name = "booleanDecision", nullable = false)
    private Boolean booleanDecision;

}

