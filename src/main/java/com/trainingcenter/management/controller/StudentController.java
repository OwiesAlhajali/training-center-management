package com.trainingcenter.management.controller;

import com.trainingcenter.management.dto.StudentCompletionPercentageDTO;
import com.trainingcenter.management.dto.StudentRequestDTO;
import com.trainingcenter.management.dto.StudentResponseDTO;
import com.trainingcenter.management.dto.StudentTrainingHoursDTO;
import com.trainingcenter.management.dto.StudentUpdateRequestDTO;
import com.trainingcenter.management.dto.WeeklyScheduleItemDTO;
import com.trainingcenter.management.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StudentResponseDTO createStudent (@Valid @RequestBody StudentRequestDTO request) {

        return studentService.createStudent(request);
    }

    @GetMapping("/{id}")
    public StudentResponseDTO getStudentById(@PathVariable Long id) {
        return studentService.getStudentById(id);
    }

    @GetMapping
    public List<StudentResponseDTO> getAllStudents() {
        return studentService.getAllStudents();
    }

    @GetMapping("/search")
    public List<StudentResponseDTO> searchStudents(@RequestParam String q, @RequestParam Long instituteId) {
        return studentService.searchStudents(q, instituteId);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public StudentResponseDTO updateStudent(
            @PathVariable Long id,
            @Valid @ModelAttribute StudentUpdateRequestDTO request) {

        return studentService.updateStudent(id, request);
    }


    @DeleteMapping("/{studentId}/register/institute/{instituteId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteStudentRegisterForInstitute(
            @PathVariable Long studentId,
            @PathVariable Long instituteId) {
    
        studentService.deleteStudentRegisterForInstitute(studentId, instituteId);
    }

    @GetMapping("/{id}/training-hours")
    public StudentTrainingHoursDTO getStudentTrainingHours(@PathVariable Long id) {
        return studentService.getStudentTrainingHours(id);
    }

    @GetMapping("/{id}/completion-percentage")
    public List<StudentCompletionPercentageDTO> getStudentCompletionPercentage(@PathVariable Long id) {
        return studentService.getStudentCompletionPercentage(id);
    }

    @GetMapping("/{id}/weekly-schedule")
    public List<WeeklyScheduleItemDTO> getStudentWeeklySchedule(
            @PathVariable Long id,
            @RequestParam(required = false) LocalDate date) {
        return studentService.getStudentWeeklySchedule(id, date);
    }

    /**
     * Updates student profile image.
     */
    @PutMapping("/{id}/profile-image")
    public StudentResponseDTO updateProfileImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) {
        return studentService.updateProfileImage(id, file);
    }
}
