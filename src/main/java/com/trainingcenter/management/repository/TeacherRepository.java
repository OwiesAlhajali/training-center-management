package com.trainingcenter.management.repository;

import com.trainingcenter.management.entity.Teacher;
import com.trainingcenter.management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    boolean existsByUserId(Long id);

    Optional<Teacher> findByUser(User user);

}
