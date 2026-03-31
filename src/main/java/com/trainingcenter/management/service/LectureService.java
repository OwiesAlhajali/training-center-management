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

        // المرحلة الأولى: فحص الجدول بالكامل وتحديد التعارضات
        while (lecturesProcessed < lecturesNeeded) {
            if (selectedDays.contains(currentDate.getDayOfWeek())) {
                
                // فحص التعارض للقاعة وللمدرس
                boolean isRoomBusy = lectureRepository.existsConflict(
                        session.getClassRoom().getId(), currentDate, startTime, endTime);
                boolean isTeacherBusy = lectureRepository.isTeacherBusy(
                        session.getTeacher().getId(), currentDate, startTime, endTime);

                if (isRoomBusy || isTeacherBusy) {
                    conflictDates.add(currentDate);
                } else {
                    // بناء المحاضرة مؤقتاً في الذاكرة
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

        // المرحلة الثانية: اتخاذ القرار (اقتراح حلول أو حفظ)
        if (!conflictDates.isEmpty()) {
            // استدعاء محرك الاقتراحات بناءً على أولوياتنا (تبديل قاعة -> إزاحة وقت)
            List<AvailableOptionDTO> suggestions = findSmartSuggestions(
                    session, startDate, startTime, endTime);

                throw new ScheduleConflictException(ConflictResponseDTO.builder()
            .message("Conflict detected on " + conflictDates.size() + " dates.")
            .conflictingDates(conflictDates)
            .suggestions(suggestions)
            .build());
        }

        // المرحلة الثالثة: الحفظ الجماعي إذا كان الجدول سليماً 100%
        lectureRepository.saveAll(pendingLectures);

        // تحديث مدة الجلسة في قاعدة البيانات
        long totalDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, currentDate);
        session.setDuration(totalDays + " days for " + lecturesNeeded + " lectures");
        // sessionRepository.save(session); // تأكد من حفظ حالة الجلسة إذا لزم الأمر
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
     * Priority 1: ROOM_SWAP (Keep time, change room based on capacity & equipment)
     * Priority 2: SERIES_SHIFT (Keep room, change time within working hours)
     */
    private List<AvailableOptionDTO> findSmartSuggestions(TrainingSession session, LocalDate date, 
                                                        LocalTime originalStart, LocalTime originalEnd) {
        List<AvailableOptionDTO> suggestions = new ArrayList<>();
        
        // 1. Define Working Hours
        LocalTime openingTime = LocalTime.of(8, 0);
        LocalTime closingTime = LocalTime.of(22, 0);
        long durationMinutes = java.time.Duration.between(originalStart, originalEnd).toMinutes();

        // 2. PRIORITY 1: Try to find another room (ROOM_SWAP)
        // We filter by capacity and equipment (simulated by course title/keywords)
        List<ClassRoom> alternativeRooms = lectureRepository.findAvailableRoomsWithFeatures(
                session.getMinSeats(), 
                session.getRequiredEquipment(),
                date, 
                originalStart, 
                originalEnd
        );

        for (ClassRoom room : alternativeRooms) {
            if (!room.getId().equals(session.getClassRoom().getId())) {
                suggestions.add(AvailableOptionDTO.builder()
                        .suggestionType("ROOM_SWAP")
                        .roomId(room.getId())
                        .roomNumber(room.getNumber())
                        .date(date)
                        .startTime(originalStart)
                        .endTime(originalEnd)
                        .note("Room " + room.getNumber() + " is available at your preferred time.")
                        .build());
            }
            if (suggestions.size() >= 2) break; // Limit to 2 room suggestions
        }

        // 3. PRIORITY 2: Try to find another time in the same room (SERIES_SHIFT)
        if (suggestions.size() < 3) {
            LocalTime scanTime = openingTime;
            
            while (scanTime.plusMinutes(durationMinutes).isBefore(closingTime)) {
                // Avoid suggesting the same original time
                if (!scanTime.equals(originalStart)) {
                    boolean isRoomFree = !lectureRepository.existsConflict(
                            session.getClassRoom().getId(), date, scanTime, scanTime.plusMinutes(durationMinutes));
                    boolean isTeacherFree = !lectureRepository.isTeacherBusy(
                            session.getTeacher().getId(), date, scanTime, scanTime.plusMinutes(durationMinutes));

                    if (isRoomFree && isTeacherFree) {
                        suggestions.add(AvailableOptionDTO.builder()
                                .suggestionType("SERIES_SHIFT")
                                .roomId(session.getClassRoom().getId())
                                .roomNumber(session.getClassRoom().getNumber())
                                .date(date)
                                .startTime(scanTime)
                                .endTime(scanTime.plusMinutes(durationMinutes))
                                .note("The entire series can be shifted to this time slot.")
                                .build());
                        break; // Suggest the first available time gap found
                    }
                }
                scanTime = scanTime.plusMinutes(30); // Scan every 30 minutes
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
