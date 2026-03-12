package com.trainingcenter.management.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InstituteResponseDTO {
    private Long id;
    private String workingHours;
    private String description;
    private String location;
    private String ownerName; //from User
}
