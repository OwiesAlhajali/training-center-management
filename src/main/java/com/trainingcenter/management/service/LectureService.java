package com.trainingcenter.management.service;

import com.trainingcenter.management.dto.LectureRequestDTO;
import com.trainingcenter.management.dto.LectureResponseDTO;
import com.trainingcenter.management.exception.ScheduleConflictException;
import com.trainingcenter.management.dto.ConflictResponseDTO;
import com.trainingcenter.management.dto.AvailableOptionDTO;
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
import java.util.ArrayList;
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

        List<LocalDate> conflictDates = new ArrayList<>();
        List<Lecture> pendingLectures = new ArrayList<>();
        
        int lecturesNeeded = session.getNumberOfLectures();
        int lecturesProcessed = 0;
        LocalDate currentDate = startDate;

        
        while (lecturesProcessed < lecturesNeeded) {
            if (selectedDays.contains(currentDate.getDayOfWeek())) {
                
        
                boolean isRoomBusy = lectureRepository.existsConflict(
                        session.getClassRoom().getId(), currentDate, startTime, endTime);
                boolean isTeacherBusy = lectureRepository.isTeacherBusy(
                        session.getTeacher().getId(), currentDate, startTime, endTime);

                if (isRoomBusy || isTeacherBusy) {
                    conflictDates.add(currentDate);
                } else {
        
                    Lecture lecture = Lecture.builder()
                            .lectureDate(currentDate)
                            .startTime(startTime)
                            .endTime(endTime)
                            .trainingSession(session)
                            .classRoom(session.getClassRoom())
                            .teacher(session.getTeacher())
                            .build();
                    pendingLectures.add(lecture);
                }
                lecturesProcessed++;
            }
            currentDate = currentDate.plusDays(1);
        }

        
        if (!conflictDates.isEmpty()) {
        
            List<AvailableOptionDTO> suggestions = findSmartSuggestions(
                    session, conflictDates, startTime, endTime);

                throw new ScheduleConflictException(ConflictResponseDTO.builder()
            .message("Conflict detected on " + conflictDates.size() + " dates.")
            .conflictingDates(conflictDates)
            .suggestions(suggestions)
            .build());
        }

        
        lectureRepository.saveAll(pendingLectures);

        
        long totalDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, currentDate);
        session.setDuration(totalDays + " days for " + lecturesNeeded + " lectures");
        
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

    /**
     * Smart Suggestion Engine: Finds alternative slots when a conflict occurs.
     * Priority 1: ROOM_SWAP (Keep time, change room for the entire series)
     * Priority 2: SERIES_SHIFT (Keep room, change time within working hours)
     * Priority 3: PARTIAL_ROOM_SWAP (Keep time, change room only for conflicted dates)
     */
   private List<AvailableOptionDTO> findSmartSuggestions(TrainingSession session, List<LocalDate> conflictDates, 
                                                        LocalTime originalStart, LocalTime originalEnd) {
    List<AvailableOptionDTO> suggestions = new ArrayList<>();
    int MAX_ALLOWED = 3; 

    LocalTime openingTime = LocalTime.of(8, 0);
    LocalTime closingTime = LocalTime.of(22, 0);
    long durationMinutes = java.time.Duration.between(originalStart, originalEnd).toMinutes();
    LocalDate referenceDate = (conflictDates != null && !conflictDates.isEmpty()) ? conflictDates.get(0) : LocalDate.now();

    List<ClassRoom> fullSwapRooms = lectureRepository.findAvailableRoomsWithFeatures(
            session.getMinSeats(), 
            session.getRequiredEquipment(),
            referenceDate, 
            originalStart, 
            originalEnd
    );

    for (ClassRoom room : fullSwapRooms) {
        if (suggestions.size() >= MAX_ALLOWED) break;
        if (!room.getId().equals(session.getClassRoom().getId())) {
            suggestions.add(AvailableOptionDTO.builder()
                    .suggestionType("ROOM_SWAP")
                    .roomId(room.getId())
                    .roomNumber(room.getNumber())
                    .date(referenceDate)
                    .startTime(originalStart)
                    .endTime(originalEnd)
                    .note("Move all sessions to Room " + room.getNumber())
                    .build());
        }
    }

    // --- الاستراتيجية 2: SERIES_SHIFT (إزاحة الوقت للسلسلة كاملة) ---
    // نبحث عن أوقات بديلة فقط إذا لم نصل لـ 3 اقتراحات بعد
    if (suggestions.size() < MAX_ALLOWED) {
        LocalTime scanTime = openingTime;
        while (scanTime.plusMinutes(durationMinutes).isBefore(closingTime) && suggestions.size() < MAX_ALLOWED) {
            if (!scanTime.equals(originalStart)) {
                boolean isRoomFree = !lectureRepository.existsConflict(
                        session.getClassRoom().getId(), referenceDate, scanTime, scanTime.plusMinutes(durationMinutes));
                boolean isTeacherFree = !lectureRepository.isTeacherBusy(
                        session.getTeacher().getId(), referenceDate, scanTime, scanTime.plusMinutes(durationMinutes));

                if (isRoomFree && isTeacherFree) {
                    suggestions.add(AvailableOptionDTO.builder()
                            .suggestionType("SERIES_SHIFT")
                            .roomId(session.getClassRoom().getId())
                            .roomNumber(session.getClassRoom().getNumber())
                            .date(referenceDate)
                            .startTime(scanTime)
                            .endTime(scanTime.plusMinutes(durationMinutes))
                            .note("Shift the entire series to: " + scanTime)
                            .build());
                }
            }
            scanTime = scanTime.plusMinutes(30); 
        }
    }



    if (suggestions.size() < MAX_ALLOWED && conflictDates != null && !conflictDates.isEmpty()) {
        List<AvailableOptionDTO> partialSuggestions = new ArrayList<>();
        boolean canResolveAllPartially = true;

        for (LocalDate confDate : conflictDates) {
            List<ClassRoom> altRooms = lectureRepository.findAvailableRoomsWithFeatures(
                    session.getMinSeats(), session.getRequiredEquipment(), confDate, originalStart, originalEnd);

            ClassRoom altRoom = altRooms.stream()
                    .filter(r -> !r.getId().equals(session.getClassRoom().getId()))
                    .findFirst().orElse(null);

            if (altRoom != null) {
                partialSuggestions.add(AvailableOptionDTO.builder()
                        .suggestionType("PARTIAL")
                        .roomId(altRoom.getId())
                        .roomNumber(altRoom.getNumber())
                        .date(confDate)
                        .startTime(originalStart)
                        .endTime(originalEnd)
                        .note("On " + confDate + ", move to " + altRoom.getNumber())
                        .build());
            } else {
                canResolveAllPartially = false;
                break;
            }
        }

        if (canResolveAllPartially && !partialSuggestions.isEmpty()) {

            suggestions.add(partialSuggestions.get(0));
        }
    }

    return suggestions;
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
