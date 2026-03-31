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

    // جلب محاضرات جلسة معينة
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<LectureResponseDTO>> getLecturesBySession(@PathVariable Long sessionId) {
        return ResponseEntity.ok(lectureService.getLecturesBySessionId(sessionId));
    }

    // جلب جميع المحاضرات (تم تصحيح النوع ليعيد DTO)[cite: 13, 18]
    @GetMapping
    public ResponseEntity<List<LectureResponseDTO>> getAllLectures() {
        return ResponseEntity.ok(lectureService.getAllLectures());
    }

    // جلب محاضرة واحدة بواسطة ID[cite: 13, 18]
    @GetMapping("/{id}")
    public ResponseEntity<LectureResponseDTO> getLectureById(@PathVariable Long id) {
        return ResponseEntity.ok(lectureService.getLectureById(id));
    }


// 1. إضافة محاضرة لجلسة معينة
// إضافة محاضرة لجلسة محددة
@PostMapping("/session/{sessionId}")
public ResponseEntity<LectureResponseDTO> addLecture(@PathVariable Long sessionId, @RequestBody LectureRequestDTO request) {
    return ResponseEntity.ok(lectureService.addLectureToSession(sessionId, request));
}

// تعديل محاضرة معينة
@PutMapping("/{id}")
public ResponseEntity<LectureResponseDTO> updateLecture(@PathVariable Long id, @RequestBody LectureRequestDTO request) {
    return ResponseEntity.ok(lectureService.updateSingleLecture(id, request));
}

// حذف محاضرة معينة
@DeleteMapping("/{id}")
public ResponseEntity<Void> deleteLecture(@PathVariable Long id) {
    lectureService.deleteSingleLecture(id);
    return ResponseEntity.noContent().build();
}



}
