package com.trainingcenter.management.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "/api/users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type")
    private UserType userType;

    @Column(name = "contact_info")
    private String contactInfo;

    private String image;

    @Column(name = "email_verified_at")
    private LocalDateTime emailVerifiedAt;

    public enum UserType {

        ADMIN,
        STUDENT,
        TEACHER

    }
}