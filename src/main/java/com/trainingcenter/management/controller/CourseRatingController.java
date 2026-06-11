package com.trainingcenter.management.controller;

import com.trainingcenter.management.dto.CourseRatingRequestDTO;
import com.trainingcenter.management.dto.CourseRatingResponseDTO;
import com.trainingcenter.management.service.CourseRatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CourseRatingController {

    private final CourseRatingService ratingService;

    @PostMapping("/courses/{courseId}/ratings")
    @ResponseStatus(HttpStatus.CREATED)
    public CourseRatingResponseDTO createRating(
            @PathVariable Long courseId,
            @RequestParam Long studentId,
            @Valid @RequestBody CourseRatingRequestDTO request
    ) {
        return ratingService.createRating(studentId, courseId, request);
    }

    @PutMapping("/ratings/{ratingId}")
    public CourseRatingResponseDTO updateRating(
            @PathVariable Long ratingId,
            @RequestParam Long studentId,
            @Valid @RequestBody CourseRatingRequestDTO request
    ) {
        return ratingService.updateRating(ratingId, studentId, request);
    }

    @DeleteMapping("/ratings/{ratingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRating(
            @PathVariable Long ratingId,
            @RequestParam Long studentId
    ) {
        ratingService.deleteRating(ratingId, studentId);
    }

    @GetMapping("/courses/{courseId}/ratings")
    public List<CourseRatingResponseDTO> getRatingsByCourse(@PathVariable Long courseId) {
        return ratingService.getRatingsByCourse(courseId);
    }

    @GetMapping("/courses/{courseId}/ratings/average")
    public BigDecimal getAverageRatingForCourse(@PathVariable Long courseId) {
        return ratingService.getAverageRatingForCourse(courseId);
    }
}