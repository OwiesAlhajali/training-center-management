package com.trainingcenter.management.controller;

import com.trainingcenter.management.dto.LectureResponseDTO;
import com.trainingcenter.management.dto.LectureRequestDTO;
import com.trainingcenter.management.service.LectureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lectures")
@RequiredArgsConstructor
public class LectureController {

    private final LectureService lectureService;

    
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<LectureResponseDTO>> getLecturesBySession(@PathVariable Long sessionId) {
        return ResponseEntity.ok(lectureService.getLecturesBySessionId(sessionId));
    }

    
    @GetMapping
    public ResponseEntity<List<LectureResponseDTO>> getAllLectures() {
        return ResponseEntity.ok(lectureService.getAllLectures());
    }

    
    @GetMapping("/{id}")
    public ResponseEntity<LectureResponseDTO> getLectureById(@PathVariable Long id) {
        return ResponseEntity.ok(lectureService.getLectureById(id));
    }




    @PostMapping("/session/{sessionId}")
    public ResponseEntity<LectureResponseDTO> addLecture(@PathVariable Long sessionId, @RequestBody LectureRequestDTO request) {
        return ResponseEntity.ok(lectureService.addLectureToSession(sessionId, request));
    }

    
    @PutMapping("/{id}")
    public ResponseEntity<LectureResponseDTO> updateLecture(@PathVariable Long id, @RequestBody LectureRequestDTO request) {
        return ResponseEntity.ok(lectureService.updateSingleLecture(id, request));
    }

    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLecture(@PathVariable Long id) {
        lectureService.deleteSingleLecture(id);
        return ResponseEntity.noContent().build();
    }



}
