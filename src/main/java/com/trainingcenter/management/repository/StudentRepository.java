package com.trainingcenter.management.repository;

import com.trainingcenter.management.entity.Student;
import com.trainingcenter.management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student,Long> {
    boolean existsByUserId(Long Id) ;
    Student findByUser(User user);
}
