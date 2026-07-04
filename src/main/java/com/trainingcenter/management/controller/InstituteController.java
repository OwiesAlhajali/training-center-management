package com.trainingcenter.management.controller;

import com.trainingcenter.management.dto.InstituteRequestDTO;
import com.trainingcenter.management.dto.InstituteResponseDTO;
import com.trainingcenter.management.dto.MonthlyFinancialPerformanceDTO;
import com.trainingcenter.management.dto.MonthlyRegistrationStatDTO;
import com.trainingcenter.management.dto.StudentResponseDTO;
import com.trainingcenter.management.service.InstituteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/institutes")
@RequiredArgsConstructor
public class InstituteController { 

    private final InstituteService instituteService;

    @PostMapping
    public ResponseEntity<InstituteResponseDTO> create(@Valid @RequestBody InstituteRequestDTO request) {
        return new ResponseEntity<>(instituteService.createInstitute(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InstituteResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(instituteService.getInstituteById(id));
    }

    
   @GetMapping("/user/{userId}")
   public ResponseEntity<List<InstituteResponseDTO>> getByUser(@PathVariable Long userId) {
       return ResponseEntity.ok(instituteService.getInstitutesByUser(userId));
   }

    @GetMapping
    public ResponseEntity<List<InstituteResponseDTO>> getAll() {
        return ResponseEntity.ok(instituteService.getAllInstitutes());
    }

    @GetMapping("/{id}/registration-monthly")
    public ResponseEntity<List<MonthlyRegistrationStatDTO>> getMonthlyRegistrations(
            @PathVariable Long id,
            @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(instituteService.getMonthlyRegistrations(id, year));
    }

    @GetMapping("/{id}/financial-monthly")
    public ResponseEntity<List<MonthlyFinancialPerformanceDTO>> getMonthlyFinancialPerformance(
            @PathVariable Long id,
            @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(instituteService.getMonthlyFinancialPerformance(id, year));
    }

    @GetMapping("/tenant/{tenantId}/students-count")
    public ResponseEntity<Long> getStudentsCountByTenant(@PathVariable Long tenantId) {
        return ResponseEntity.ok(instituteService.getStudentsCountByTenant(tenantId));
    }

    @GetMapping("/tenant/{tenantId}/students")
    public ResponseEntity<List<StudentResponseDTO>> getStudentsByTenant(@PathVariable Long tenantId) {
        return ResponseEntity.ok(instituteService.getStudentsByTenant(tenantId));
    }

    @GetMapping("/tenant/{tenantId}/students/active")
    public ResponseEntity<List<StudentResponseDTO>> getActiveStudentsByTenant(@PathVariable Long tenantId) {
        return ResponseEntity.ok(instituteService.getActiveStudentsByTenant(tenantId));
    }

    @GetMapping("/tenant/{tenantId}/students/active/count")
    public ResponseEntity<Long> getActiveStudentsCountByTenant(@PathVariable Long tenantId) {
        return ResponseEntity.ok(instituteService.getActiveStudentsCountByTenant(tenantId));
    }
    
    @GetMapping("/{id}/users/count")
    public ResponseEntity<Long> getTotalUsersCountByInstitute(@PathVariable Long id) {
        return ResponseEntity.ok(instituteService.getTotalUsersCountByInstitute(id));
    }

    @GetMapping("/{id}/training-sessions/active/count")
    public ResponseEntity<Long> getActiveTrainingSessionsCountByInstitute(@PathVariable Long id) {
        return ResponseEntity.ok(instituteService.getActiveTrainingSessionsCountByInstitute(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InstituteResponseDTO> update(@PathVariable Long id, @Valid @RequestBody InstituteRequestDTO request) {
        return ResponseEntity.ok(instituteService.updateInstitute(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        instituteService.deleteInstitute(id);
        return ResponseEntity.noContent().build();
    }
}
