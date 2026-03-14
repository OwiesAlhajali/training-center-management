package com.trainingcenter.management.service;

import com.trainingcenter.management.dto.ClassRoomRequestDTO;
import com.trainingcenter.management.dto.ClassRoomResponseDTO;
import com.trainingcenter.management.entity.ClassRoom;
import com.trainingcenter.management.entity.Institute;
import com.trainingcenter.management.exception.ResourceNotFoundException;
import com.trainingcenter.management.repository.ClassRoomRepository;
import com.trainingcenter.management.repository.InstituteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassRoomService {

    private final ClassRoomRepository classRoomRepository;
    private final InstituteRepository instituteRepository;

    public ClassRoomResponseDTO createClassRoom(ClassRoomRequestDTO requestDTO) {
        Institute institute = instituteRepository.findById(requestDTO.getInstituteId())
                .orElseThrow(() -> new ResourceNotFoundException("Institute not found with ID: " + requestDTO.getInstituteId()));

        ClassRoom classroom = ClassRoom.builder()
                .number(requestDTO.getNumber())
                .capacity(requestDTO.getCapacity())
                .availableDevices(requestDTO.getAvailableDevices())
                .images(requestDTO.getImages())
                .institute(institute)
                .build();

        return mapToResponse(classRoomRepository.save(classroom));
    }

    public ClassRoomResponseDTO getClassRoomById(Long id) {
        ClassRoom classroom = classRoomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Classroom not found with ID: " + id));
        return mapToResponse(classroom);
    }

    public List<ClassRoomResponseDTO> getAllByInstitute(Long instituteId) {
        if (!instituteRepository.existsById(instituteId)) {
            throw new ResourceNotFoundException("Institute not found");
        }
        return classRoomRepository.findByInstituteId(instituteId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ClassRoomResponseDTO updateClassRoom(Long id, ClassRoomRequestDTO requestDTO) {
        ClassRoom existing = classRoomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Classroom not found with ID: " + id));

        existing.setNumber(requestDTO.getNumber());
        existing.setCapacity(requestDTO.getCapacity());
        existing.setAvailableDevices(requestDTO.getAvailableDevices());
        existing.setImages(requestDTO.getImages());

        return mapToResponse(classRoomRepository.save(existing));
    }

    public void deleteClassRoom(Long id) {
        if (!classRoomRepository.existsById(id)) {
            throw new ResourceNotFoundException("Classroom not found");
        }
        classRoomRepository.deleteById(id);
    }

    private ClassRoomResponseDTO mapToResponse(ClassRoom classroom) {
        return ClassRoomResponseDTO.builder()
                .id(classroom.getId())
                .number(classroom.getNumber())
                .capacity(classroom.getCapacity())
                .availableDevices(classroom.getAvailableDevices())
                .images(classroom.getImages())
                .instituteId(classroom.getInstitute().getId())
		.instituteName(classroom.getInstitute().getDescription())
                .build();
    }
}
