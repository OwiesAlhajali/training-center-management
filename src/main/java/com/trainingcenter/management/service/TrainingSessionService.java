package com.trainingcenter.management.service;

import com.trainingcenter.management.dto.TrainingSessionRequestDTO;
import com.trainingcenter.management.dto.TrainingSessionResponseDTO;
import com.trainingcenter.management.entity.*;
import com.trainingcenter.management.exception.BadRequestException;
import com.trainingcenter.management.exception.ResourceNotFoundException;
import com.trainingcenter.management.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrainingSessionService {

    private final TrainingSessionRepository sessionRepository;
    private final CourseRepository courseRepository;
    private final ClassRoomRepository classRoomRepository;
    private final TeacherRepository teacherRepository;
    private final LectureService lectureService;


    public TrainingSessionResponseDTO getSessionById(Long id) {
        TrainingSession session = sessionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Training Session not found with ID: " + id));
       return mapToResponse(session);
    }
  

    public List<TrainingSessionResponseDTO> getAllSessions() {
        return sessionRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<TrainingSessionResponseDTO> getByInstitute(Long instituteId) {
        return sessionRepository.findByInstituteId(instituteId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<TrainingSessionResponseDTO> getByTenant(Long tenantId) {
        return sessionRepository.findByTenantId(tenantId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    //method to validate session capacity 

    private void validateSessionCapacity(Integer requestedSeats, Long classroomId) {
        ClassRoom classroom = classRoomRepository.findById(classroomId)
            .orElseThrow(() -> new ResourceNotFoundException("Classroom not found with ID: " + classroomId));
            
        if (requestedSeats > classroom.getCapacity()) {
        throw new BadRequestException("Capacity Conflict: Requested seats (" + requestedSeats + 
                ") exceed classroom capacity (" + classroom.getCapacity() + ") for room: " + classroom.getNumber());
       }
    }
    
    @Transactional
    public TrainingSessionResponseDTO createSession(TrainingSessionRequestDTO requestDTO) {

    validateSessionCapacity(requestDTO.getAvailableSeats(), requestDTO.getClassroomId());

    Course course = courseRepository.findById(requestDTO.getCourseId())
            .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
    ClassRoom classroom = classRoomRepository.findById(requestDTO.getClassroomId()).get();
    Teacher teacher = teacherRepository.findById(requestDTO.getTeacherId())
            .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));

TrainingSession session = TrainingSession.builder()
            .price(requestDTO.getPrice())
            .availableSeats(requestDTO.getAvailableSeats())
            .minSeats(requestDTO.getMinSeats())
            .numberOfLectures(requestDTO.getNumberOfLectures())
            .status(requestDTO.getStatus())
            .course(course)
            .classRoom(classroom)
            .teacher(teacher)
            .requiredEquipment(requestDTO.getRequiredEquipment())
            .build();

    TrainingSession savedSession = sessionRepository.save(session);

   if (requestDTO.getStartDate() != null && requestDTO.getDaysOfWeek() != null) {
        lectureService.generateAutoLectures(savedSession, requestDTO.getStartDate(), 
                                           requestDTO.getStartTime(), requestDTO.getEndTime(), 
                                           requestDTO.getDaysOfWeek());
    }

    return mapToResponse(savedSession);


    }

@Transactional
public TrainingSessionResponseDTO updateSession(Long id, TrainingSessionRequestDTO requestDTO) {
    TrainingSession existingSession = sessionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Training Session not found with ID: " + id));

    // تحديث البيانات الأساسية للجلسة
    existingSession.setPrice(requestDTO.getPrice());
    existingSession.setAvailableSeats(requestDTO.getAvailableSeats());
    existingSession.setMinSeats(requestDTO.getMinSeats());
    existingSession.setNumberOfLectures(requestDTO.getNumberOfLectures());
    existingSession.setStatus(requestDTO.getStatus());
    existingSession.setRequiredEquipment(requestDTO.getRequiredEquipment());

    // تحديث العلاقات إذا تغيرت
    if (!existingSession.getClassRoom().getId().equals(requestDTO.getClassroomId())) {
        existingSession.setClassRoom(classRoomRepository.findById(requestDTO.getClassroomId())
                .orElseThrow(() -> new ResourceNotFoundException("Classroom not found")));
    }
    
    if (!existingSession.getTeacher().getId().equals(requestDTO.getTeacherId())) {
        existingSession.setTeacher(teacherRepository.findById(requestDTO.getTeacherId())
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found")));
    }

    // --- الجزء الخاص بالمحاضرات ---
    // إذا تم إرسال بيانات مواعيد جديدة، نقوم بتحديث جدول المحاضرات
    if (requestDTO.getStartDate() != null && requestDTO.getDaysOfWeek() != null) {
        // حذف المواعيد القديمة
        lectureService.removeLecturesBySession(id);
        
        // توليد المواعيد الجديدة (سيقوم بفحص التعارضات تلقائياً)
        lectureService.generateAutoLectures(
            existingSession, 
            requestDTO.getStartDate(), 
            requestDTO.getStartTime(), 
            requestDTO.getEndTime(), 
            requestDTO.getDaysOfWeek()
        );
    }

    return mapToResponse(sessionRepository.save(existingSession));
}

@Transactional
public void deleteSession(Long id) {
    // 1. التحقق من وجود الجلسة
    TrainingSession session = sessionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Training Session not found with ID: " + id));

    // 2. حذف المحاضرات المرتبطة بالجلسة أولاً
    lectureService.removeLecturesBySession(id);

    // 3. حذف الجلسة نفسها
    sessionRepository.delete(session);
}

    private TrainingSessionResponseDTO mapToResponse(TrainingSession session) {
        return TrainingSessionResponseDTO.builder()
                .id(session.getId())
                .price(session.getPrice())
                .availableSeats(session.getAvailableSeats())
                .minSeats(session.getMinSeats())
                .numberOfLectures(session.getNumberOfLectures())
		.requiredEquipment(session.getRequiredEquipment())
                .duration(session.getDuration())
                .status(session.getStatus())
                .courseName(session.getCourse().getName())
                .classroomName(session.getClassRoom().getNumber())
                .teacherName(session.getTeacher().getUser().getUsername())
                .instituteName(session.getClassRoom().getInstitute().getName())
                .build();
    }
}
