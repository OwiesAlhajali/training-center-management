package com.trainingcenter.management.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
@Data
@Builder
public class StudentRequestDTO {

    private Long userId;

    private String firstName;

    private String lastName;

    private String gender;

    private LocalDate birthDate;

    private String address;

    private String interest;
}
