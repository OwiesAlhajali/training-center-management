package com.trainingcenter.management.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class OtpRequestDTO {
    private String email;
}