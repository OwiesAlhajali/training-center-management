package com.trainingcenter.management.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TenantRequestDTO {
    @NotBlank(message = "Tenant key is required")
    private String key;

    @NotBlank(message = "Tenant name is required")
    private String name;

    private String address;
}
