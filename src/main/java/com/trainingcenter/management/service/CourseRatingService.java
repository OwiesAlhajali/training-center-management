package com.trainingcenter.management.service;
import com.trainingcenter.management.dto.CourseRatingRequestDTO;
import com.trainingcenter.management.dto.CourseRatingResponseDTO;
import com.trainingcenter.management.entity.Course;
import com.trainingcenter.management.entity.CourseRating;
import com.trainingcenter.management.entity.Student;
import com.trainingcenter.management.exception.ResourceNotFoundException;
import com.trainingcenter.management.repository.CourseRatingRepository;
import com.trainingcenter.management.repository.CourseRepository;
import com.trainingcenter.management.repository.EnrollmentRepository;
import com.trainingcenter.management.repository.StudentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CourseRatingService {

    private final CourseRatingRepository ratingRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Transactional
    public CourseRatingResponseDTO createRating(Long studentId, Long courseId, CourseRatingRequestDTO dto) {

        validateRating(dto.getRating());

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        boolean isEnrolled = enrollmentRepository
                .existsByStudentIdAndTrainingSession_CourseId(studentId, courseId);

        if (!isEnrolled) {
            throw new IllegalArgumentException("Student is not enrolled in this course");
        }

        if (ratingRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new IllegalArgumentException("Student already rated this course");
        }

        CourseRating rating = new CourseRating();
        rating.setStudent(student);
        rating.setCourse(course);
        rating.setRating(dto.getRating());
        rating.setReview(dto.getReview());

        return mapToDTO(ratingRepository.save(rating));
    }

    @Transactional
    public CourseRatingResponseDTO updateRating(Long ratingId, Long studentId, CourseRatingRequestDTO dto) {

        validateRating(dto.getRating());

        CourseRating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new ResourceNotFoundException("Rating not found"));

        if (!rating.getStudent().getId().equals(studentId)) {
            throw new IllegalArgumentException("You are not allowed to update this rating");
        }

        rating.setRating(dto.getRating());
        rating.setReview(dto.getReview());

        return mapToDTO(ratingRepository.save(rating));
    }

    @Transactional
    public void deleteRating(Long ratingId, Long studentId) {

        CourseRating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new ResourceNotFoundException("Rating not found"));

        if (!rating.getStudent().getId().equals(studentId)) {
            throw new IllegalArgumentException("You are not allowed to delete this rating");
        }

        ratingRepository.delete(rating);
    }

    private void validateRating(BigDecimal rating) {
        if (rating == null ||
                rating.compareTo(BigDecimal.ONE) < 0 ||
                rating.compareTo(BigDecimal.valueOf(5)) > 0) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
    }

    private CourseRatingResponseDTO mapToDTO(CourseRating rating) {
        return new CourseRatingResponseDTO(
                rating.getId(),
                rating.getCourse().getId(),
                rating.getCourse().getName(),
                rating.getStudent().getId(),
                rating.getStudent().getUser().getUsername(),
                rating.getRating(),
                rating.getReview()
        );
    }
}