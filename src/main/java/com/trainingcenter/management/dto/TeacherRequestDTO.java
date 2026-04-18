package com.trainingcenter.management.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeacherRequestDTO {

    // Backward-compatibility for existing flow that links to an existing user.
    private Long userId;

    // Unified registration flow fields.
    private String username;
    private String email;
    private String password;
    private String confirmPassword;
    private String phone;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Specialization is required")
    private String specialization;

    private String certificates;

    private String address;

    private String cv;

    @JsonAlias("yearsOfExperience")
    @Min(value = 0, message = "Experience must be >= 0")
    private Integer experienceYears;
}