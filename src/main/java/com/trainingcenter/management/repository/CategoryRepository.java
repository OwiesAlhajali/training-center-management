package com.trainingcenter.management.repository;

import com.trainingcenter.management.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category,Long> {
    boolean existsByName(String name) ;
}
