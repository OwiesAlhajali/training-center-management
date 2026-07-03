package com.trainingcenter.management.repository;

import com.trainingcenter.management.entity.Student;
import com.trainingcenter.management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface StudentRepository extends JpaRepository<Student,Long> {
    boolean existsByUserId(Long id);

    @Query("SELECT s FROM Student s JOIN s.user u WHERE u.username LIKE %:keyword% OR s.firstName LIKE %:keyword% OR s.lastName LIKE %:keyword%")
    List<Student> searchByUsernameOrName(@Param("keyword") String keyword);

    @Query("SELECT s FROM Student s JOIN s.user u " +
           "WHERE (u.username LIKE %:keyword% OR s.firstName LIKE %:keyword% OR s.lastName LIKE %:keyword%) " +
           "AND s.id IN (SELECT r.student.id FROM Register r WHERE r.tenant.id = " +
           "  (SELECT i.tenant.id FROM Institute i WHERE i.id = :instituteId))")
    List<Student> searchByUsernameOrNameAndInstituteId(@Param("keyword") String keyword, @Param("instituteId") Long instituteId);

    Optional<Student> findByUser(User user);

    Optional<Student> findByUserId(Long userId);
}
