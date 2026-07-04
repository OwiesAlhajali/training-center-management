package com.trainingcenter.management.repository;

import com.trainingcenter.management.entity.InstituteTeacher;
import com.trainingcenter.management.entity.TeacherInstituteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstituteTeacherRepository extends JpaRepository<InstituteTeacher, Long> {

    List<InstituteTeacher> findByInstituteId(Long instituteId);

    List<InstituteTeacher> findByTeacherId(Long teacherId);

    Optional<InstituteTeacher> findByInstituteIdAndTeacherId(Long instituteId, Long teacherId);

    boolean existsByInstituteIdAndTeacherId(Long instituteId, Long teacherId);

    @Query("SELECT it FROM InstituteTeacher it WHERE it.institute.id = :instituteId AND it.status = :status")
    List<InstituteTeacher> findByInstituteIdAndStatus(@Param("instituteId") Long instituteId,
                                                      @Param("status") TeacherInstituteStatus status);

    @Query("SELECT it.teacher.id FROM InstituteTeacher it WHERE it.institute.id = :instituteId")
    List<Long> findTeacherIdsByInstituteId(@Param("instituteId") Long instituteId);
}
