package com.trainingcenter.management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
public class UserResponseDTO {

    private Long id;
    private String username;
    private String email;
    private String userType;
    private String contactInfo;
    private String image;

}