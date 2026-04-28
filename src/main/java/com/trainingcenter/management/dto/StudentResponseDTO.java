package com.trainingcenter.management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentResponseDTO {

    private Long id;

    private String firstName;

    private String lastName;

    private String gender;

    private LocalDate birthDate;

    private String address;

    private String interest;

    private LocalDate enrollmentDate;

    // User info
    private Long userId;
    private String username;
    private String email;
    private String contactInfo;
    private String image;
}
