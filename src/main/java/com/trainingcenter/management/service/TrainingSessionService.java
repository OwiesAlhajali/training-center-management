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
            .duration(requestDTO.getDuration())
            .status(requestDTO.getStatus())
            .course(course)
            .classRoom(classroom)
            .teacher(teacher)
            .build();

       return mapToResponse(sessionRepository.save(session));
    }

    @Transactional
   public TrainingSessionResponseDTO updateSession(Long id, TrainingSessionRequestDTO requestDTO) {
    TrainingSession existingSession = sessionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Session not found with ID: " + id));

    validateSessionCapacity(requestDTO.getAvailableSeats(), requestDTO.getClassroomId());


    existingSession.setPrice(requestDTO.getPrice());
    existingSession.setAvailableSeats(requestDTO.getAvailableSeats());
    existingSession.setMinSeats(requestDTO.getMinSeats());
    existingSession.setNumberOfLectures(requestDTO.getNumberOfLectures());
    existingSession.setDuration(requestDTO.getDuration());
    existingSession.setStatus(requestDTO.getStatus());

  
    if (!existingSession.getCourse().getId().equals(requestDTO.getCourseId())) {
        existingSession.setCourse(courseRepository.findById(requestDTO.getCourseId()).get());
    }
    if (!existingSession.getClassRoom().getId().equals(requestDTO.getClassroomId())) {
        existingSession.setClassRoom(classRoomRepository.findById(requestDTO.getClassroomId()).get());
    }
    if (!existingSession.getTeacher().getId().equals(requestDTO.getTeacherId())) {
        existingSession.setTeacher(teacherRepository.findById(requestDTO.getTeacherId()).get());
    }

    return mapToResponse(sessionRepository.save(existingSession));
 }

    @Transactional
    public void deleteSession(Long id) {
        if (!sessionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Training Session not found");
        }
        sessionRepository.deleteById(id);
    }

    private TrainingSessionResponseDTO mapToResponse(TrainingSession session) {
        return TrainingSessionResponseDTO.builder()
                .id(session.getId())
                .price(session.getPrice())
                .availableSeats(session.getAvailableSeats())
                .minSeats(session.getMinSeats())
                .numberOfLectures(session.getNumberOfLectures())
                .duration(session.getDuration())
                .status(session.getStatus())
                .courseName(session.getCourse().getName())
                .classroomName(session.getClassRoom().getNumber())
                .teacherName(session.getTeacher().getUser().getUsername())
                .instituteName(session.getClassRoom().getInstitute().getName())
                .build();
    }
}
