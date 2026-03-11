package org.hartford.swasthraksha.model;

import jakarta.persistence.Entity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class ClaimDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String documentType;

    private String fileUrl;

    private LocalDateTime uploadedAt;

    @ManyToOne
    @JoinColumn(name = "claim_id")
    private Claim claim;
}
