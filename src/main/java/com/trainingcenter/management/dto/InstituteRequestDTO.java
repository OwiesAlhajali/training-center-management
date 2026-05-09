package com.trainingcenter.management.dto;

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

    @NotNull(message = "Tenant ID is required")
    private Long tenantId;

    @NotBlank(message = "Institute name is required")
    private String name;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    private String description;
    private String location;
    private String phoneNumber;
    private String email;
    private List<String> workingDays;
    private InstituteStatus status;
}
