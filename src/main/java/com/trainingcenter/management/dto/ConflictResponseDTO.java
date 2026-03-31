package com.trainingcenter.management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConflictResponseDTO {
    private String message;
    private List<LocalDate> conflictingDates;
    private List<AvailableOptionDTO> suggestions;
}
