package com.trainingcenter.management.dto;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailableOptionDTO {
    private String suggestionType; // ROOM_SWAP, SERIES_SHIFT, or PARTIAL
    private Long roomId;
    private String roomNumber;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String note;
}
