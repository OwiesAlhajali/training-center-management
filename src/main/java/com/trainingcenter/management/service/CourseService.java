package com.trainingcenter.management.service;

import com.trainingcenter.management.dto.CourseRequestDTO;
import com.trainingcenter.management.dto.CourseResponseDTO;
import com.trainingcenter.management.entity.Category;
import com.trainingcenter.management.entity.Course;
import com.trainingcenter.management.entity.Tenant;
import com.trainingcenter.management.repository.CategoryRepository;
import com.trainingcenter.management.repository.CourseRepository;
import com.trainingcenter.management.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {

    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;
    private final TenantRepository tenantRepository;

   @Transactional
   public CourseResponseDTO createCourse(CourseRequestDTO dto) {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Error: Category with ID " + dto.getCategoryId() + " not found."));

        Tenant tenant = tenantRepository.findById(dto.getTenantId())
                .orElseThrow(() -> new RuntimeException("Error: Tenant with ID " + dto.getTenantId() + " not found."));

        Course course = Course.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .requirements(dto.getRequirements())
                .hours(dto.getHours())
                .category(category)
                .tenant(tenant)
                .build();

        return mapToResponse(courseRepository.save(course));
    }
	
    public CourseResponseDTO getCourseById(Long id) {
	    Course course = courseRepository.findById(id)
		    .orElseThrow(() -> new RuntimeException("Course not found with id: " + id));
	    return mapToResponse(course);
 }

    public List<CourseResponseDTO> searchCoursesByName(String name, Long tenantId) {
      return courseRepository.findByNameContainingIgnoreCaseAndTenantId(name, tenantId).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }    

   public List<CourseResponseDTO> getCoursesByCategoryAndTenant(Long categoryId, Long tenantId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new RuntimeException("Category not found");
        }
        if (!tenantRepository.existsById(tenantId)) {
            throw new RuntimeException("Tenant not found");
        }

        return courseRepository.findByCategoryIdAndTenantId(categoryId, tenantId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<CourseResponseDTO> getCoursesByTenant(Long tenantId) {
        return courseRepository.findByTenantId(tenantId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

   @Transactional
   public CourseResponseDTO updateCourse(Long id, CourseRequestDTO dto) {
   
    Course course = courseRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Course not found"));


    course.setName(dto.getName());
    course.setDescription(dto.getDescription());
    course.setRequirements(dto.getRequirements());
    course.setHours(dto.getHours());

    if (dto.getCategoryId() != null) {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        course.setCategory(category); 
    } else {
        course.setCategory(null); 
    }

     return mapToResponse(courseRepository.save(course));
   }
   @Transactional
   public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new RuntimeException("Cannot delete. Course not found");
        }
        courseRepository.deleteById(id);
    }

    private CourseResponseDTO mapToResponse(Course course) {
        return CourseResponseDTO.builder()
                .id(course.getId())
                .name(course.getName())
                .description(course.getDescription())
                .requirements(course.getRequirements())
                .hours(course.getHours())
                .categoryName(course.getCategory().getName())
                .tenantName(course.getTenant().getName())
                .build();
    }
}
