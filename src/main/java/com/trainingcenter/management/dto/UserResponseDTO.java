package com.trainingcenter.management.dto;

import com.trainingcenter.management.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDTO {

    private Long id;
    private String username;
    private String email;
    private User.UserType  userType;
    private String contactInfo;
    private String image;

}