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
    private String workingHours; // formatted: "HH:mm - HH:mm"
    private String description;
    private String location;
    private String phoneNumber;
    private String email;
    private List<String> workingDays;
    private InstituteStatus status;
    private Long userId;
    private String ownerName; //from User
    private Long tenantId;
    private String tenantName; // from Tenant
}
