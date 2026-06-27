package com.trainingcenter.management.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.trainingcenter.management.entity.InstituteStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;
import java.util.List;

@Data
public class InstituteRequestDTO {
    @NotNull(message = "User ID is required")
    private Long userId;

   
    private Long tenantId;

    @NotBlank(message = "Institute name is required")
    private String name;

    @NotBlank(message = "Location is required")
    private String location;

    private String description;

    private String phoneNumber;

    private String email;

    @NotNull(message = "Start time is required")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime endTime;

    private List<String> workingDays;
    private InstituteStatus status;
}
