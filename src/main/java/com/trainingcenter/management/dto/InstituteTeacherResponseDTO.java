package com.trainingcenter.management.dto;

import com.trainingcenter.management.entity.TeacherInstituteStatus;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstituteTeacherResponseDTO {

    private Long id;

    private Long instituteId;
    private String instituteName;

    private Long teacherId;
    private String teacherName;
    private String teacherUsername;
    private String teacherEmail;
    private String teacherSpecialization;
    private String teacherPhone;
    private String teacherImage;

    private TeacherInstituteStatus status;

    private LocalDate joinedDate;
}
