package com.trainingcenter.management.controller;

import com.trainingcenter.management.dto.LoginRequestDTO;
import com.trainingcenter.management.dto.LoginResponseDTO;
import com.trainingcenter.management.entity.User;
import com.trainingcenter.management.exception.ResourceNotFoundException;
import com.trainingcenter.management.repository.UserRepository;
import com.trainingcenter.management.repository.StudentRepository;
import com.trainingcenter.management.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class LoginController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;

    /**
     * Simple login endpoint
     * In production, replace with JWT token generation
     * For now, returns user details on successful authentication
     */
 @PostMapping("/login")
 public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO request) {
     User user = userRepository.findByEmail(request.getEmail())
             .orElseThrow(() -> new ResourceNotFoundException("Invalid email or password"));

    // Verify password
     if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
         throw new ResourceNotFoundException("Invalid email or password");
     }

     LoginResponseDTO.LoginResponseDTOBuilder builder = LoginResponseDTO.builder()
             .id(user.getId())
             .username(user.getUsername())
             .email(user.getEmail())
             .userType(user.getUserType())
             .message("Login successful");

     if (user.getUserType() == User.UserType.STUDENT) {
         studentRepository.findByUser(user)
                 .ifPresent(student -> builder.studentId(student.getId()));
     } 
     else if (user.getUserType() == User.UserType.TEACHER) {
         teacherRepository.findByUser(user)
                 .ifPresent(teacher -> builder.teacherId(teacher.getId()));
     }

     return ResponseEntity.ok(builder.build());
  }
}

