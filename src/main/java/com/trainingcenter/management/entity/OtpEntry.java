package com.trainingcenter.management.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "otp_entries")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class OtpEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email; 

    @Column(nullable = false)
    private String code;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    @Builder.Default
    @Column(name = "is_used")
    private boolean used = false;
}