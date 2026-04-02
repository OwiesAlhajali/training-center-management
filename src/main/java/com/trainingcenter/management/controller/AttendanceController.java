package com.trainingcenter.management.controller;

import com.trainingcenter.management.dto.AttendanceResponseDTO;
import com.trainingcenter.management.dto.BulkAttendanceRequestDTO;
import com.trainingcenter.management.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    
    @PostMapping("/bulk")
    public ResponseEntity<String> saveAttendance(@RequestBody BulkAttendanceRequestDTO request) {
        attendanceService.markBulkAttendance(request);
        return ResponseEntity.ok("Attendance processed successfully for " + request.getRecords().size() + " students.");
    }

   
    @GetMapping("/lecture/{lectureId}")
    public ResponseEntity<List<AttendanceResponseDTO>> getByLecture(@PathVariable Long lectureId) {
        List<AttendanceResponseDTO> response = attendanceService.getAttendanceByLecture(lectureId);
        return ResponseEntity.ok(response);
    }

  
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<AttendanceResponseDTO>> getByStudent(@PathVariable Long studentId) {
        List<AttendanceResponseDTO> response = attendanceService.getAttendanceByStudent(studentId);
        return ResponseEntity.ok(response);
    }
}
