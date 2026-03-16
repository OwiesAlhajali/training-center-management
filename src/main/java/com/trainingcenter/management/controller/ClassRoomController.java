package com.trainingcenter.management.controller;

import com.trainingcenter.management.dto.ClassRoomRequestDTO;
import com.trainingcenter.management.dto.ClassRoomResponseDTO;
import com.trainingcenter.management.service.ClassRoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classrooms")
@RequiredArgsConstructor
public class ClassRoomController {

    private final ClassRoomService classRoomService;

    @PostMapping
    public ResponseEntity<ClassRoomResponseDTO> create(@Valid @RequestBody ClassRoomRequestDTO request) {
        return new ResponseEntity<>(classRoomService.createClassRoom(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClassRoomResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(classRoomService.getClassRoomById(id));
    }

    @GetMapping("/institute/{instituteId}")
    public ResponseEntity<List<ClassRoomResponseDTO>> getByInstitute(@PathVariable Long instituteId) {
        return ResponseEntity.ok(classRoomService.getAllByInstitute(instituteId));
    }

    @GetMapping("/search/device")
    public ResponseEntity<List<ClassRoomResponseDTO>> getByDevice(@RequestParam String device, @RequestParam Long instituteId) {
           return ResponseEntity.ok(classRoomService.searchByDevice(device, instituteId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClassRoomResponseDTO> update(@PathVariable Long id, @Valid @RequestBody ClassRoomRequestDTO request) {
        return ResponseEntity.ok(classRoomService.updateClassRoom(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        classRoomService.deleteClassRoom(id);
        return ResponseEntity.noContent().build();
    }
}
