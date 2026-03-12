package com.trainingcenter.management.service;

import com.trainingcenter.management.dto.InstituteRequestDTO;
import com.trainingcenter.management.dto.InstituteResponseDTO;
import com.trainingcenter.management.entity.Institute;
import com.trainingcenter.management.entity.User;
import com.trainingcenter.management.exception.ResourceNotFoundException;
import com.trainingcenter.management.repository.InstituteRepository;
import com.trainingcenter.management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InstituteService {

    private final InstituteRepository instituteRepository;
    private final UserRepository userRepository;

    public InstituteResponseDTO createInstitute(InstituteRequestDTO requestDTO) {
        User user = userRepository.findById(requestDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + requestDTO.getUserId()));

        Institute institute = Institute.builder()
                .workingHours(requestDTO.getWorkingHours())
                .description(requestDTO.getDescription())
                .location(requestDTO.getLocation())
                .user(user)
                .build();

        return mapToResponse(instituteRepository.save(institute));
    }

    public InstituteResponseDTO getInstituteById(Long id) {
        Institute institute = instituteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Institute not found"));
        return mapToResponse(institute);
    }

    // --- الميثودات التي كانت ناقصة وتسببت في الخطأ ---

    public List<InstituteResponseDTO> getAllInstitutes() {
        return instituteRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public InstituteResponseDTO updateInstitute(Long id, InstituteRequestDTO requestDTO) {
        Institute existing = instituteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Institute not found with ID: " + id));

        existing.setWorkingHours(requestDTO.getWorkingHours());
        existing.setDescription(requestDTO.getDescription());
        existing.setLocation(requestDTO.getLocation());

        return mapToResponse(instituteRepository.save(existing));
    }

    public void deleteInstitute(Long id) {
        if (!instituteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Institute not found with ID: " + id);
        }
        instituteRepository.deleteById(id);
    }

    // --- ميثود التحويل المساعدة ---

    private InstituteResponseDTO mapToResponse(Institute institute) {
        return InstituteResponseDTO.builder()
                .id(institute.getId())
                .workingHours(institute.getWorkingHours())
                .description(institute.getDescription())
                .location(institute.getLocation())
                .ownerName(institute.getUser() != null ? institute.getUser().getUsername() : "No Owner")
                .build();
    }
}
