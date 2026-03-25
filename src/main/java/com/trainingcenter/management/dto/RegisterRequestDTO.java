package com.trainingcenter.management.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDTO {

    @NotNull(message = "studentId is required")
    private Long studentId;

    @NotNull(message = "tenantId is required")
    private Long tenantId;
}