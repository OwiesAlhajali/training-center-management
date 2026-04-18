package com.trainingcenter.management.controller;

import com.trainingcenter.management.dto.CourseRatingRequestDTO;
import com.trainingcenter.management.dto.CourseRatingResponseDTO;
import com.trainingcenter.management.service.CourseRatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CourseRatingController {

    private final CourseRatingService ratingService;

    // Create Rating
    @PostMapping("/courses/{courseId}/ratings")
    @ResponseStatus(HttpStatus.CREATED)
    public CourseRatingResponseDTO createRating(
            @PathVariable Long courseId,
            @RequestParam Long studentId,
            @Valid @RequestBody CourseRatingRequestDTO request
    ) {
        return ratingService.createRating(studentId, courseId, request);
    }

    // Update Rating
    @PutMapping("/ratings/{ratingId}")
    public CourseRatingResponseDTO updateRating(
            @PathVariable Long ratingId,
            @RequestParam Long studentId,
            @Valid @RequestBody CourseRatingRequestDTO request
    ) {
        return ratingService.updateRating(ratingId, studentId, request);
    }

    // Delete Rating
    @DeleteMapping("/ratings/{ratingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRating(
            @PathVariable Long ratingId,
            @RequestParam Long studentId
    ) {
        ratingService.deleteRating(ratingId, studentId);
    }
}