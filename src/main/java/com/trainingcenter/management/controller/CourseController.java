package com.trainingcenter.management.controller;

import com.trainingcenter.management.dto.CourseRequestDTO;
import com.trainingcenter.management.dto.CourseResponseDTO;
import com.trainingcenter.management.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    public ResponseEntity<CourseResponseDTO> create(@RequestBody CourseRequestDTO dto) {
        return ResponseEntity.ok(courseService.createCourse(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseResponseDTO> getById(@PathVariable Long id) {
    	return ResponseEntity.ok(courseService.getCourseById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<CourseResponseDTO>> searchByName(@RequestParam String name, @RequestParam Long tenantId) {
    
    	List<CourseResponseDTO> results = courseService.searchCoursesByName(name, tenantId);
    	return ResponseEntity.ok(results);
    }

    @GetMapping("/category/{categoryId}/tenant/{tenantId}")
    public ResponseEntity<List<CourseResponseDTO>> getByCategoryAndTenant(@PathVariable Long categoryId, @PathVariable Long tenantId) {
	 List<CourseResponseDTO> courses = courseService.getCoursesByCategoryAndTenant(categoryId, tenantId);
	 return ResponseEntity.ok(courses);
    }

    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<List<CourseResponseDTO>> getByTenant(@PathVariable Long tenantId) {
        return ResponseEntity.ok(courseService.getCoursesByTenant(tenantId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseResponseDTO> update(@PathVariable Long id, @RequestBody CourseRequestDTO dto) {
        return ResponseEntity.ok(courseService.updateCourse(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }
}
