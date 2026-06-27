package com.trainingcenter.management.repository;

import com.trainingcenter.management.entity.Student;
import com.trainingcenter.management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    boolean existsByUserId(Long id);

    Optional<Student> findByUser(User user);

    Optional<Student> findByUserId(Long userId);
}
