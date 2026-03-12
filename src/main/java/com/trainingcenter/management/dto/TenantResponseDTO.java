
package com.trainingcenter.management.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TenantResponseDTO {
    private Long id;
    private String key;
    private String name;
    private String address;
}

