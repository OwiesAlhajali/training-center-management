package com.trainingcenter.management.dto;

import com.trainingcenter.management.entity.TeacherInstituteStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InstituteTeacherRequestDTO {

    @NotNull(message = "Institute ID is required")
    private Long instituteId;

    @NotNull(message = "Teacher ID is required")
    private Long teacherId;

    private TeacherInstituteStatus status;

    private LocalDate joinedDate;
}
