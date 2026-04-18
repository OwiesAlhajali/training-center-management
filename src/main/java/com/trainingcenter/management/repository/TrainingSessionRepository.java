package com.trainingcenter.management.repository;

import com.trainingcenter.management.entity.TrainingSession;
import com.trainingcenter.management.entity.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface TrainingSessionRepository extends JpaRepository<TrainingSession, Long> {

    @Query("SELECT ts FROM TrainingSession ts WHERE ts.classRoom.institute.id = :instituteId")
    List<TrainingSession> findByInstituteId(@Param("instituteId") Long instituteId);

    @Query("SELECT ts FROM TrainingSession ts WHERE ts.course.tenant.id = :tenantId")
    List<TrainingSession> findByTenantId(@Param("tenantId") Long tenantId);

    List<TrainingSession> findByCourseId(Long courseId);

    @Query("""
            SELECT ts FROM TrainingSession ts
            JOIN ts.course c
            JOIN c.category cat
            JOIN ts.classRoom cr
            JOIN cr.institute inst
            WHERE (:categoryId IS NULL OR cat.id = :categoryId)
              AND (:categoryName IS NULL OR LOWER(cat.name) = LOWER(:categoryName))
              AND (:instituteName IS NULL OR LOWER(inst.name) LIKE LOWER(CONCAT('%', :instituteName, '%')))
              AND (:location IS NULL OR LOWER(inst.location) LIKE LOWER(CONCAT('%', :location, '%')))
              AND (:minPrice IS NULL OR ts.price >= :minPrice)
              AND (:maxPrice IS NULL OR ts.price <= :maxPrice)
            """)
    List<TrainingSession> findWithFilters(@Param("categoryId") Long categoryId,
                                          @Param("categoryName") String categoryName,
                                          @Param("instituteName") String instituteName,
                                          @Param("location") String location,
                                          @Param("minPrice") BigDecimal minPrice,
                                          @Param("maxPrice") BigDecimal maxPrice);

    @Query("""
            SELECT ts.course.id,
                   ts.course.name,
                   SUM(CASE WHEN ts.status = :completedStatus THEN 1 ELSE 0 END),
                   COUNT(ts)
            FROM TrainingSession ts
            WHERE ts.teacher.id = :teacherId
              AND ts.status <> :cancelledStatus
            GROUP BY ts.course.id, ts.course.name
            """)
    List<Object[]> getTeacherCourseProgress(@Param("teacherId") Long teacherId,
                                            @Param("completedStatus") SessionStatus completedStatus,
                                            @Param("cancelledStatus") SessionStatus cancelledStatus);

    List<TrainingSession> findByStatus(SessionStatus status);
}
