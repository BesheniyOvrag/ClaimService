package com.example.claim_checker.entity;

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

    private String policyType;
    private String date;

    @Column(length = 2000)
    private String description;

    @Column(length = 2000)
    private String decision;
}

