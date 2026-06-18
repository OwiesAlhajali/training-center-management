package com.trainingcenter.management.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    @JsonFormat(pattern = "hh:mm a")
    private LocalTime startTime;

    @JsonFormat(pattern = "hh:mm a")
    private LocalTime endTime;

    private String description;
    private String location;
    private String phoneNumber;
    private String email;
    private List<String> workingDays;
    private InstituteStatus status;
    private Long userId;
    private String ownerName;
    private Long tenantId;
    private String tenantName;
    private String tenantKey;
}