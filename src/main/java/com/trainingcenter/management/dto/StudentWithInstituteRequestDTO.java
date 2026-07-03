package com.trainingcenter.management.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentWithInstituteRequestDTO {

    @Valid
    @NotNull(message = "Student data is required")
    private StudentRequestDTO student;

    @NotNull(message = "Institute ID is required")
    private Long instituteId;
}
