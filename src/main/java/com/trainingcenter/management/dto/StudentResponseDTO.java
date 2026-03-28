package com.trainingcenter.management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDate;

@Data
@Builder
@Getter
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

    private Long userId;
}
