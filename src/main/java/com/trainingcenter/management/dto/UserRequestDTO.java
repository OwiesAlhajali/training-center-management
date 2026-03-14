package com.trainingcenter.management.dto;

import com.trainingcenter.management.entity.User;
import lombok.Data;

@Data
public class UserRequestDTO {

    private String username;
    private String email;
    private String password;
    private User.UserType userType;
    private String contactInfo;
    private String image;
}
