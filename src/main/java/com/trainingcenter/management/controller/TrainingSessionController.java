package com.trainingcenter.management.controller;

import com.trainingcenter.management.dto.TrainingSessionRequestDTO;
import com.trainingcenter.management.dto.TrainingSessionResponseDTO;
import com.trainingcenter.management.service.TrainingSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/training-sessions")
@RequiredArgsConstructor
public class TrainingSessionController {

    private final TrainingSessionService sessionService;

    @PostMapping
    public ResponseEntity<TrainingSessionResponseDTO> createSession(
            @Valid @RequestBody TrainingSessionRequestDTO requestDTO) {
        TrainingSessionResponseDTO createdSession = sessionService.createSession(requestDTO);
        return new ResponseEntity<>(createdSession, HttpStatus.CREATED);
    }


	@GetMapping("/{id}")
	public ResponseEntity<TrainingSessionResponseDTO> getSessionById(@PathVariable Long id) {
	    return ResponseEntity.ok(sessionService.getSessionById(id));
	}


    @GetMapping("/sessions-with-filter")
    public ResponseEntity<List<TrainingSessionResponseDTO>> getAllSessions(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String instituteName,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String location) {
        return ResponseEntity.ok(
                sessionService.getSessionsWithFilters(category, instituteName, minPrice, maxPrice, location)
        );
    }

    @GetMapping("/institute/{instituteId}")
    public ResponseEntity<List<TrainingSessionResponseDTO>> getByInstitute(@PathVariable Long instituteId) {
        return ResponseEntity.ok(sessionService.getByInstitute(instituteId));
    }

    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<List<TrainingSessionResponseDTO>> getByTenant(@PathVariable Long tenantId) {
        return ResponseEntity.ok(sessionService.getByTenant(tenantId));
    }

    @GetMapping("/active")
    public ResponseEntity<List<TrainingSessionResponseDTO>> getActiveSessions() {
        return ResponseEntity.ok(sessionService.getActiveSessions());
    }

    @PutMapping("/{id}")
    public ResponseEntity<TrainingSessionResponseDTO> updateSession(
            @PathVariable Long id, 
            @Valid @RequestBody TrainingSessionRequestDTO requestDTO) {
        return ResponseEntity.ok(sessionService.updateSession(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long id) {
        sessionService.deleteSession(id);
        return ResponseEntity.noContent().build();
    }
}
