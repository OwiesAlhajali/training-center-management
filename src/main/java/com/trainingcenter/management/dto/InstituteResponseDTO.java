package com.trainingcenter.management.dto;

import com.trainingcenter.management.entity.InstituteStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;
import java.util.List;

@Data
@Builder
public class InstituteResponseDTO {
    private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private String description;
    private String address;
    private String phoneNumber;
    private String email;
    private List<String> workingDays;
    private InstituteStatus status;
    private Long userId;
    private String ownerName; //from User
    private Long tenantId;
    private String tenantName; // from Tenant
}
