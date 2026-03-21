package com.trainingcenter.management.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherResponseDTO {

    private Long id;

    private String firstName;

    private String lastName;

    private String specialization;

    private String certificates;

    private String address;

    private String cv;

    private Integer experienceYears;

    private Long userId;
}