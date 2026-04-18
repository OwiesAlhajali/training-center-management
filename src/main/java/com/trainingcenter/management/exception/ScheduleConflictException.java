package com.trainingcenter.management.exception;

import com.trainingcenter.management.dto.ConflictResponseDTO;
import lombok.Getter;

@Getter
public class ScheduleConflictException extends RuntimeException {
    private final ConflictResponseDTO conflictResponse;

    public ScheduleConflictException(ConflictResponseDTO conflictResponse) {
        super(conflictResponse.getMessage());
        this.conflictResponse = conflictResponse;
    }
}