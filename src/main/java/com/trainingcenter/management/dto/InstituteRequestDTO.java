package com.trainingcenter.management.dto;

import com.trainingcenter.management.entity.InstituteStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class InstituteRequestDTO {
    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Tenant ID is required")
    private Long tenantId;

    @NotBlank(message = "Institute name is required")
    private String name;

    @NotBlank(message = "Working hours are required")
    private String workingHours;

    private String description;
    private String address;
    private String phoneNumber;
    private String email;
    private List<String> workingDays;
    private InstituteStatus status;
}
