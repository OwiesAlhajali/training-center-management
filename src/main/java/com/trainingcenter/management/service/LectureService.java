package com.trainingcenter.management.service;

import com.trainingcenter.management.dto.LectureRequestDTO;
import com.trainingcenter.management.dto.LectureResponseDTO;
import com.trainingcenter.management.entity.ClassRoom;
import com.trainingcenter.management.entity.Lecture;
import com.trainingcenter.management.entity.Teacher;
import com.trainingcenter.management.entity.TrainingSession;
import com.trainingcenter.management.exception.BadRequestException;
import com.trainingcenter.management.exception.ResourceNotFoundException;
import com.trainingcenter.management.repository.ClassRoomRepository;
import com.trainingcenter.management.repository.LectureRepository;
import com.trainingcenter.management.repository.TeacherRepository;
import com.trainingcenter.management.repository.TrainingSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LectureService {

    private final LectureRepository lectureRepository;
    private final TrainingSessionRepository sessionRepository;
    private final ClassRoomRepository classRoomRepository;
    private final TeacherRepository teacherRepository;

    // Get lectures by session ID
    public List<LectureResponseDTO> getLecturesBySessionId(Long sessionId) {
        return lectureRepository.findByTrainingSession_Id(sessionId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // Get all lectures in the system
    public List<LectureResponseDTO> getAllLectures() {
        return lectureRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // Get single lecture by ID
    public LectureResponseDTO getLectureById(Long id) {
        Lecture lecture = lectureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lecture not found with ID: " + id));
        return mapToResponseDTO(lecture);
    }

    // Auto generate lectures based on session schedule
    @Transactional
    public void generateAutoLectures(TrainingSession session, LocalDate startDate, 
                                    LocalTime startTime, LocalTime endTime, List<String> days) {
        
        List<DayOfWeek> selectedDays = days.stream()
                .map(String::toUpperCase)
                .map(DayOfWeek::valueOf)
                .collect(Collectors.toList());

        int lecturesCreated = 0;
        LocalDate currentDate = startDate;

        while (lecturesCreated < session.getNumberOfLectures()) {
            if (selectedDays.contains(currentDate.getDayOfWeek())) {
                
                validateConflicts(session.getClassRoom().getId(), session.getTeacher().getId(), 
                                 currentDate, startTime, endTime);

                Lecture lecture = Lecture.builder()
                        .lectureDate(currentDate)
                        .startTime(startTime)
                        .endTime(endTime)
                        .trainingSession(session)
                        .classRoom(session.getClassRoom())
                        .teacher(session.getTeacher())
                        .build();

                lectureRepository.save(lecture);
                lecturesCreated++;
            }
            currentDate = currentDate.plusDays(1);
        }

       long weeks = ChronoUnit.WEEKS.between(startDate, currentDate);
       session.setDuration(weeks + " weeks (" + session.getNumberOfLectures() + " lectures)");
    }

    // Add a single manual lecture to a session
    @Transactional
    public LectureResponseDTO addLectureToSession(Long sessionId, LectureRequestDTO request) {
        TrainingSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        validateConflicts(request.getClassroomId(), request.getTeacherId(), 
                         request.getLectureDate(), request.getStartTime(), request.getEndTime());

        ClassRoom room = classRoomRepository.findById(request.getClassroomId())
                .orElseThrow(() -> new ResourceNotFoundException("Classroom not found"));
        Teacher teacher = teacherRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));

        Lecture lecture = Lecture.builder()
                .lectureDate(request.getLectureDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .trainingSession(session)
                .classRoom(room)
                .teacher(teacher)
                .build();

        return mapToResponseDTO(lectureRepository.save(lecture));
    }

    // Update single lecture details
    @Transactional
    public LectureResponseDTO updateSingleLecture(Long lectureId, LectureRequestDTO request) {
        Lecture existingLecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new ResourceNotFoundException("Lecture not found"));

        validateConflicts(request.getClassroomId(), request.getTeacherId(), 
                         request.getLectureDate(), request.getStartTime(), request.getEndTime());

        existingLecture.setLectureDate(request.getLectureDate());
        existingLecture.setStartTime(request.getStartTime());
        existingLecture.setEndTime(request.getEndTime());
        
        existingLecture.setClassRoom(classRoomRepository.findById(request.getClassroomId())
                .orElseThrow(() -> new ResourceNotFoundException("Classroom not found")));
        existingLecture.setTeacher(teacherRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found")));

        return mapToResponseDTO(lectureRepository.save(existingLecture));
    }

    // Delete single lecture
    @Transactional
    public void deleteSingleLecture(Long lectureId) {
        if (!lectureRepository.existsById(lectureId)) {
            throw new ResourceNotFoundException("Lecture not found");
        }
        lectureRepository.deleteById(lectureId);
    }

    // Remove all lectures belonging to a session
    @Transactional
    public void removeLecturesBySession(Long sessionId) {
        lectureRepository.deleteByTrainingSession_Id(sessionId);
    }

    // Conflict validation logic
    private void validateConflicts(Long roomId, Long teacherId, LocalDate date, LocalTime start, LocalTime end) {
        if (lectureRepository.existsConflict(roomId, date, start, end)) {
            throw new BadRequestException("Conflict: Classroom is occupied on " + date);
        }
        if (lectureRepository.isTeacherBusy(teacherId, date, start, end)) {
            throw new BadRequestException("Conflict: Teacher is busy on " + date);
        }
    }

    // Mapper entity to DTO
    private LectureResponseDTO mapToResponseDTO(Lecture lecture) {
        return LectureResponseDTO.builder()
                .id(lecture.getId())
                .lectureDate(lecture.getLectureDate())
                .startTime(lecture.getStartTime())
                .endTime(lecture.getEndTime())
                .sessionName(lecture.getTrainingSession().getCourse().getName())
                .classroomNumber(lecture.getClassRoom().getNumber())
                .teacherName(lecture.getTeacher().getFirstName() + " " + lecture.getTeacher().getLastName())
                .build();
    }
}
