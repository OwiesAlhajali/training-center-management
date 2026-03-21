package com.trainingcenter.management.repository;

import com.trainingcenter.management.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherRepository extends JpaRepository<Teacher,Long> {
    boolean existsByUserId (Long id) ;
}
