package com.trainingcenter.management.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ResetPasswordRequestDTO {
    private String email;
    private String newPassword;
}
