package com.trainingcenter.management.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClassRoomResponseDTO {
    private Long id;
    private String number;
    private Integer capacity;
    private String availableDevices;
    private String images;
    private Long instituteId;
    private String instituteName; 
}