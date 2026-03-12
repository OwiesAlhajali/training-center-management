package com.trainingcenter.management.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    private UserType userType;

    private String contactInfo;

    private String image;

    private LocalDateTime emailVerifiedAt;

}

enum UserType {

    ADMIN,
    TEACHER,
    STUDENT

}