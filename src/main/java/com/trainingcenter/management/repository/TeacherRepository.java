package com.trainingcenter.management.repository;

import com.trainingcenter.management.entity.Teacher;
import com.trainingcenter.management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface TeacherRepository extends JpaRepository<Teacher,Long> {
    boolean existsByUserId(Long id);

    @Query("SELECT t FROM Teacher t JOIN t.user u WHERE u.username LIKE %:keyword% OR t.firstName LIKE %:keyword% OR t.lastName LIKE %:keyword%")
    List<Teacher> searchByUsernameOrName(@Param("keyword") String keyword);

    @Query("SELECT t FROM Teacher t JOIN t.user u " +
           "WHERE (u.username LIKE %:keyword% OR t.firstName LIKE %:keyword% OR t.lastName LIKE %:keyword%) " +
           "AND t.id IN (SELECT ts.teacher.id FROM TrainingSession ts JOIN ts.classRoom cr " +
           "  WHERE cr.institute.id = :instituteId)")
    List<Teacher> searchByUsernameOrNameAndInstituteId(@Param("keyword") String keyword, @Param("instituteId") Long instituteId);

    @Query("SELECT t FROM Teacher t JOIN t.user u " +
           "WHERE t.id IN (SELECT it.teacher.id FROM InstituteTeacher it WHERE it.institute.id = :instituteId)")
    List<Teacher> findByInstituteId(@Param("instituteId") Long instituteId);

    @Query("SELECT t FROM Teacher t JOIN t.user u " +
           "WHERE (u.username LIKE %:keyword% OR t.firstName LIKE %:keyword% OR t.lastName LIKE %:keyword%) " +
           "AND t.id IN (SELECT it.teacher.id FROM InstituteTeacher it WHERE it.institute.id = :instituteId)")
    List<Teacher> searchByInstituteId(@Param("keyword") String keyword, @Param("instituteId") Long instituteId);

    Optional<Teacher> findByUser(User user);

}
