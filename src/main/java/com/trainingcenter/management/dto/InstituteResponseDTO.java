package com.trainingcenter.management.dto;

import com.trainingcenter.management.entity.InstituteStatus;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class InstituteResponseDTO {
    private Long id;
    private String name;
    private String workingHours;
    private String description;
    private String location;
    private String phoneNumber;
    private String email;
    private List<String> workingDays;
    private InstituteStatus status;
    private String ownerName; //from User
    // email and contactInfo intentionally omitted (sensitive / moved to User responses)
    private String tenantName; // from Tenant
}
