package com.trainingcenter.management.service;

import com.trainingcenter.management.dto.LectureRequestDTO;
import com.trainingcenter.management.dto.LectureResponseDTO;
import com.trainingcenter.management.exception.ScheduleConflictException;
import com.trainingcenter.management.dto.ConflictResponseDTO;
import com.trainingcenter.management.dto.AvailableOptionDTO;
import com.trainingcenter.management.entity.ClassRoom;
import com.trainingcenter.management.entity.Institute;
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
    @Transactional(readOnly = true)
    public List<LectureResponseDTO> getLecturesBySessionId(Long sessionId) {
        return lectureRepository.findByTrainingSession_Id(sessionId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // Get all lectures in the system
    @Transactional(readOnly = true)
    public List<LectureResponseDTO> getAllLectures() {
        return lectureRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // Get single lecture by ID
    @Transactional(readOnly = true)
    public LectureResponseDTO getLectureById(Long id) {
        Lecture lecture = lectureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lecture not found with ID: " + id));
        return mapToResponseDTO(lecture);
    }

    // Auto generate lectures based on session schedule
    @Transactional
    public void generateAutoLectures(TrainingSession session, LocalDate startDate,
                                     LocalTime startTime, LocalTime endTime, List<String> days) {

       
        if (days == null || days.isEmpty()) {
            throw new BadRequestException("At least one day of the week must be selected");
        }

        ClassRoom classRoom = classRoomRepository.findById(session.getClassRoom().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Classroom not found"));
        validateWithinInstituteHours(classRoom, startTime, endTime);

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
                        classRoom.getId(), currentDate, startTime, endTime);
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
                            .classRoom(classRoom)
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

        LocalDate lastLectureDate = currentDate.minusDays(1);
        long totalDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, lastLectureDate) + 1;
        session.setDuration(totalDays + " days for " + lecturesNeeded + " lectures");
    }

    // Add a single manual lecture to a session
    @Transactional
    public LectureResponseDTO addLectureToSession(Long sessionId, LectureRequestDTO request) {
        TrainingSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        ClassRoom room = classRoomRepository.findById(request.getClassroomId())
                .orElseThrow(() -> new ResourceNotFoundException("Classroom not found"));
        validateWithinInstituteHours(room, request.getStartTime(), request.getEndTime());

        validateConflicts(request.getClassroomId(), request.getTeacherId(),
                request.getLectureDate(), request.getStartTime(), request.getEndTime());

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
     *
     * Priority 1: ROOM_SWAP   — Keep the same time, swap to a different room for ALL conflict dates.
     *                           A room is only suggested if it is free on EVERY conflicting date.
     * Priority 2: SERIES_SHIFT — Keep the same room, shift the time within working hours.
     *                            A time slot is only suggested if both room and teacher are free
     *                            on ALL conflicting dates.
     * Priority 3: PARTIAL     — Keep the same time, swap room only for each individual conflict date.
     *                           All conflict-date suggestions are included (up to MAX_ALLOWED).
     */
    private List<AvailableOptionDTO> findSmartSuggestions(TrainingSession session,
                                                           List<LocalDate> conflictDates,
                                                           LocalTime originalStart,
                                                           LocalTime originalEnd) {
        List<AvailableOptionDTO> suggestions = new ArrayList<>();
        int MAX_ALLOWED = 3;

        Institute institute = session.getClassRoom().getInstitute();
        LocalTime openingTime = institute.getStartTime();
        LocalTime closingTime = institute.getEndTime();
        long durationMinutes = java.time.Duration.between(originalStart, originalEnd).toMinutes();
        LocalDate referenceDate = conflictDates.get(0);
        Long instituteId = institute.getId();

        // ── Priority 1: ROOM_SWAP ──────────────────────────────────────────────────────
        List<ClassRoom> sameInstituteRooms = classRoomRepository.findByInstituteId(instituteId);

        for (ClassRoom room : sameInstituteRooms) {
            if (suggestions.size() >= MAX_ALLOWED) break;
            if (room.getId().equals(session.getClassRoom().getId())) continue;
            if (room.getCapacity() < session.getMinSeats()) continue;
            if (session.getRequiredEquipment() != null && !session.getRequiredEquipment().isBlank()
                    && (room.getAvailableDevices() == null
                    || !room.getAvailableDevices().toLowerCase()
                    .contains(session.getRequiredEquipment().toLowerCase()))) {
                continue;
            }

            // Check availability on ALL conflict dates (not just the first one)
            boolean freeOnAllConflictDates = conflictDates.stream().allMatch(date ->
                    !lectureRepository.existsConflict(room.getId(), date, originalStart, originalEnd)
            );

            if (freeOnAllConflictDates) {
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

        // ── Priority 2: SERIES_SHIFT ───────────────────────────────────────────────────
        if (suggestions.size() < MAX_ALLOWED) {
            LocalTime scanTime = openingTime;
            while (scanTime.plusMinutes(durationMinutes).compareTo(closingTime) <= 0
                    && suggestions.size() < MAX_ALLOWED) {

                if (!scanTime.equals(originalStart)) {
                    final LocalTime slotStart = scanTime;
                    final LocalTime slotEnd = scanTime.plusMinutes(durationMinutes);

                    // Check availability on ALL conflict dates
                    boolean allDatesFreeForShift = conflictDates.stream().allMatch(date ->
                            !lectureRepository.existsConflict(
                                    session.getClassRoom().getId(), date, slotStart, slotEnd)
                                    && !lectureRepository.isTeacherBusy(
                                    session.getTeacher().getId(), date, slotStart, slotEnd)
                    );

                    if (allDatesFreeForShift) {
                        suggestions.add(AvailableOptionDTO.builder()
                                .suggestionType("SERIES_SHIFT")
                                .roomId(session.getClassRoom().getId())
                                .roomNumber(session.getClassRoom().getNumber())
                                .date(referenceDate)
                                .startTime(slotStart)
                                .endTime(slotEnd)
                                .note("Shift the entire series to: " + slotStart)
                                .build());
                    }
                }
                scanTime = scanTime.plusMinutes(30);
            }
        }

        // ── Priority 3: PARTIAL ────────────────────────────────────────────────────────
        if (suggestions.size() < MAX_ALLOWED) {
            List<AvailableOptionDTO> partialSuggestions = new ArrayList<>();
            boolean canResolveAllPartially = true;

            for (LocalDate confDate : conflictDates) {
                List<ClassRoom> altRooms = lectureRepository.findAvailableRoomsWithFeatures(
                        instituteId,
                        session.getMinSeats(),
                        session.getRequiredEquipment(),
                        confDate,
                        originalStart,
                        originalEnd);

                ClassRoom altRoom = altRooms.stream()
                        .filter(r -> !r.getId().equals(session.getClassRoom().getId()))
                        .findFirst()
                        .orElse(null);

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

            // Add ALL partial suggestions up to MAX_ALLOWED (was only adding the first one)
            if (canResolveAllPartially && !partialSuggestions.isEmpty()) {
                int remaining = MAX_ALLOWED - suggestions.size();
                partialSuggestions.stream()
                        .limit(remaining)
                        .forEach(suggestions::add);
            }
        }

        return suggestions;
    }

    // Update single lecture details
    @Transactional
    public LectureResponseDTO updateSingleLecture(Long lectureId, LectureRequestDTO request) {
        Lecture existingLecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new ResourceNotFoundException("Lecture not found"));

        ClassRoom room = classRoomRepository.findById(request.getClassroomId())
                .orElseThrow(() -> new ResourceNotFoundException("Classroom not found"));
        validateWithinInstituteHours(room, request.getStartTime(), request.getEndTime());

        validateConflicts(request.getClassroomId(), request.getTeacherId(),
                request.getLectureDate(), request.getStartTime(), request.getEndTime());

        existingLecture.setLectureDate(request.getLectureDate());
        existingLecture.setStartTime(request.getStartTime());
        existingLecture.setEndTime(request.getEndTime());

        existingLecture.setClassRoom(room);
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
    private void validateConflicts(Long roomId, Long teacherId, LocalDate date,
                                   LocalTime start, LocalTime end) {
        if (lectureRepository.existsConflict(roomId, date, start, end)) {
            throw new BadRequestException("Conflict: Classroom is occupied on " + date);
        }
        if (lectureRepository.isTeacherBusy(teacherId, date, start, end)) {
            throw new BadRequestException("Conflict: Teacher is busy on " + date);
        }
    }

    private void validateWithinInstituteHours(ClassRoom classRoom, LocalTime start, LocalTime end) {
        if (start == null || end == null) {
            throw new BadRequestException("Lecture start and end times are required");
        }

        if (!end.isAfter(start)) {
            throw new BadRequestException("Lecture end time must be after start time");
        }

        Institute institute = classRoom.getInstitute();
        if (institute == null) {
            throw new ResourceNotFoundException(
                    "Institute not found for classroom ID: " + classRoom.getId());
        }

        LocalTime instituteStart = institute.getStartTime();
        LocalTime instituteEnd = institute.getEndTime();
        if (instituteStart == null || instituteEnd == null) {
            throw new BadRequestException(
                    "Institute working hours are not configured for classroom ID: " + classRoom.getId());
        }

        if (start.isBefore(instituteStart) || end.isAfter(instituteEnd)) {
            throw new BadRequestException("Lecture time must be within institute working hours: "
                    + instituteStart + " - " + instituteEnd);
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
