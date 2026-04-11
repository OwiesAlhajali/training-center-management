package com.trainingcenter.management.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class OtpVerificationDTO {
    private String email;
    private String code;
}