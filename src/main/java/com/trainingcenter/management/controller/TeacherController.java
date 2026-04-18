package com.trainingcenter.management.controller;

import com.trainingcenter.management.dto.TeacherRequestDTO;
import com.trainingcenter.management.dto.TeacherResponseDTO;
import com.trainingcenter.management.dto.TeacherCourseProgressDTO;
import com.trainingcenter.management.dto.WeeklyScheduleItemDTO;
import com.trainingcenter.management.service.TeacherService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;


    @PostMapping
    public ResponseEntity<TeacherResponseDTO> createTeacher(
            @Valid @RequestBody TeacherRequestDTO requestDTO) {

        TeacherResponseDTO response = teacherService.createTeacher(requestDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @GetMapping
    public ResponseEntity<List<TeacherResponseDTO>> getAllTeachers() {

        return ResponseEntity.ok(teacherService.getAllTeachers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeacherResponseDTO> getTeacherById(
            @PathVariable Long id) {

        return ResponseEntity.ok(teacherService.getTeacherById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TeacherResponseDTO> updateTeacher(
            @PathVariable Long id,
            @Valid @RequestBody TeacherRequestDTO requestDTO) {

        TeacherResponseDTO response = teacherService.updateTeacher(id, requestDTO);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeacher(
            @PathVariable Long id) {

        teacherService.deleteTeacher(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/course-progress")
    public ResponseEntity<List<TeacherCourseProgressDTO>> getTeacherCourseProgress(@PathVariable Long id) {
        return ResponseEntity.ok(teacherService.getTeacherCourseProgress(id));
    }

    @GetMapping("/{id}/weekly-schedule")
    public ResponseEntity<List<WeeklyScheduleItemDTO>> getTeacherWeeklySchedule(@PathVariable Long id) {
        return ResponseEntity.ok(teacherService.getTeacherWeeklySchedule(id));
    }
}