package com.trainingcenter.management.dto;

import com.trainingcenter.management.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class LoginResponseDTO {
    private Long id;
    private String username;
    private String email;
    private User.UserType userType;
    private String message;
}

