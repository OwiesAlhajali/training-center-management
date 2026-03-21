package com.trainingcenter.management.repository;

import com.trainingcenter.management.entity.ClassRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ClassRoomRepository extends JpaRepository<ClassRoom, Long> {
    List<ClassRoom> findByInstituteId(Long instituteId);
    List<ClassRoom> findByAvailableDevicesContainingIgnoreCaseAndInstituteId(String device, Long instituteId);
}
