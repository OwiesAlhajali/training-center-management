package com.trainingcenter.management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClassRoomRequestDTO {
    @NotBlank(message = "Classroom number is required")
    private String number;

    private Integer capacity;
    private String availableDevices;
    private String images;

    @NotNull(message = "Institute ID is required")
    private Long instituteId;
}