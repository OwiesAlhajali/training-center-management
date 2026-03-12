package com.trainingcenter.management.dto;

import lombok.Data;

@Data
public class UserRequestDTO {

    private String username;
    private String email;
    private String password;
    private String userType;
    private String contactInfo;
    private String image;
}
