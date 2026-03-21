package com.trainingcenter.management.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeacherRequestDTO {

    @NotNull(message = "User id is required")
    private Long userId;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Specialization is required")
    private String specialization;

    private String certificates;

    private String address;

    private String cv;

    @Min(value = 0, message = "Experience must be >= 0")
    private Integer experienceYears;
}