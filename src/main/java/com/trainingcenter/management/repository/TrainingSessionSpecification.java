package com.trainingcenter.management.repository;

import com.trainingcenter.management.entity.TrainingSession;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TrainingSessionSpecification {

    /**
     * Builds a dynamic Specification for filtering based on provided parameters.
     * Automatically ignores parameters with null values.
     * 
     * @param categoryId Category ID (optional)
     * @param categoryName Category name (optional)
     * @param courseName Course name (optional)
     * @param instituteName Institute name (optional)
     * @param location Location (optional)
     * @param minPrice Minimum price (optional)
     * @param maxPrice Maximum price (optional)
     * @return Specification that can be applied to the query
     */
    public static Specification<TrainingSession> withFilters(
            Long categoryId,
            String categoryName,
            String courseName,
            String instituteName,
            String location,
            BigDecimal minPrice,
            BigDecimal maxPrice) {
        
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // ===== Category Filter =====
            // If we have the category ID (number)
            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(
                    root.join("course").join("category").get("id"), 
                    categoryId));
            } 
            // Otherwise, if we have the category name (text)
            else if (categoryName != null && !categoryName.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(
                    criteriaBuilder.lower(root.join("course").join("category").get("name")),
                    categoryName.toLowerCase()));
            }
            
            // ===== Course Name Filter =====
            if (courseName != null && !courseName.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.join("course").get("name")),
                    "%" + courseName.toLowerCase() + "%"));
            }
            
            // ===== Institute Name Filter =====
            if (instituteName != null && !instituteName.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.join("classRoom").join("institute").get("name")),
                    "%" + instituteName.toLowerCase() + "%"));
            }
            
            // ===== Location Filter =====
            if (location != null && !location.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.join("classRoom").join("institute").get("location")),
                    "%" + location.toLowerCase() + "%"));
            }
            
            // ===== Minimum Price Filter =====
            if (minPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("price"), minPrice));
            }
            
            // ===== Maximum Price Filter =====
            if (maxPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("price"), maxPrice));
            }
            
            // Combine all predicates using AND logic
            // If the predicates list is empty, null will be returned (no restrictions)
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
