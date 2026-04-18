package com.trainingcenter.management.controller;

import com.trainingcenter.management.dto.StudentCompletionPercentageDTO;
import com.trainingcenter.management.dto.StudentRequestDTO;
import com.trainingcenter.management.dto.StudentResponseDTO;
import com.trainingcenter.management.dto.StudentTrainingHoursDTO;
import com.trainingcenter.management.dto.WeeklyScheduleItemDTO;
import com.trainingcenter.management.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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

    @PutMapping("/{id}")
    public StudentResponseDTO updateStudent(
            @PathVariable Long id,
            @Valid @RequestBody StudentRequestDTO request) {

        return studentService.updateStudent(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
    }

    @GetMapping("/{id}/training-hours")
    public StudentTrainingHoursDTO getStudentTrainingHours(@PathVariable Long id) {
        return studentService.getStudentTrainingHours(id);
    }

    @GetMapping("/{id}/completion-percentage")
    public StudentCompletionPercentageDTO getStudentCompletionPercentage(@PathVariable Long id) {
        return studentService.getStudentCompletionPercentage(id);
    }

    @GetMapping("/{id}/weekly-schedule")
    public List<WeeklyScheduleItemDTO> getStudentWeeklySchedule(@PathVariable Long id) {
        return studentService.getStudentWeeklySchedule(id);
    }
}