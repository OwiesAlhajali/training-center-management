package com.trainingcenter.management.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterResponseDTO {

    private Long id;

    private Long studentId;

    private Long tenantId;

    private BigDecimal balance;
}
