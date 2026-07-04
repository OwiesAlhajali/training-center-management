package com.trainingcenter.management.controller;

import com.trainingcenter.management.dto.InstituteTeacherRequestDTO;
import com.trainingcenter.management.dto.InstituteTeacherResponseDTO;
import com.trainingcenter.management.entity.TeacherInstituteStatus;
import com.trainingcenter.management.service.InstituteTeacherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/institute-teachers")
@RequiredArgsConstructor
public class InstituteTeacherController {

    private final InstituteTeacherService instituteTeacherService;

    @PostMapping
    public ResponseEntity<InstituteTeacherResponseDTO> assignTeacherToInstitute(
            @Valid @RequestBody InstituteTeacherRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(instituteTeacherService.assignTeacherToInstitute(requestDTO));
    }

    @GetMapping("/institute/{instituteId}/teachers")
    public ResponseEntity<List<InstituteTeacherResponseDTO>> getTeachersByInstitute(
            @PathVariable Long instituteId) {
        return ResponseEntity.ok(instituteTeacherService.getTeachersByInstitute(instituteId));
    }

    @GetMapping("/institute/{instituteId}/teachers/active")
    public ResponseEntity<List<InstituteTeacherResponseDTO>> getActiveTeachersByInstitute(
            @PathVariable Long instituteId) {
        return ResponseEntity.ok(instituteTeacherService.getActiveTeachersByInstitute(instituteId));
    }

    @GetMapping("/teacher/{teacherId}/institutes")
    public ResponseEntity<List<InstituteTeacherResponseDTO>> getInstitutesByTeacher(
            @PathVariable Long teacherId) {
        return ResponseEntity.ok(instituteTeacherService.getInstitutesByTeacher(teacherId));
    }

    @PatchMapping("/institute/{instituteId}/teacher/{teacherId}/status")
    public ResponseEntity<InstituteTeacherResponseDTO> updateTeacherStatus(
            @PathVariable Long instituteId,
            @PathVariable Long teacherId,
            @RequestParam TeacherInstituteStatus status) {
        return ResponseEntity.ok(
                instituteTeacherService.updateTeacherInstituteStatus(instituteId, teacherId, status));
    }

    @DeleteMapping("/institute/{instituteId}/teacher/{teacherId}")
    public ResponseEntity<Void> removeTeacherFromInstitute(
            @PathVariable Long instituteId,
            @PathVariable Long teacherId) {
        instituteTeacherService.removeTeacherFromInstitute(instituteId, teacherId);
        return ResponseEntity.noContent().build();
    }
}
