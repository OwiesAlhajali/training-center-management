package com.trainingcenter.management.service;
import com.trainingcenter.management.dto.StudentRequestDTO;
import com.trainingcenter.management.dto.StudentResponseDTO;
import com.trainingcenter.management.entity.Student;
import com.trainingcenter.management.entity.User;
import com.trainingcenter.management.exception.BadRequestException;
import com.trainingcenter.management.exception.DuplicateResourceException;
import com.trainingcenter.management.exception.ResourceNotFoundException;
import com.trainingcenter.management.repository.StudentRepository;
import com.trainingcenter.management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

    public StudentResponseDTO createStudent(StudentRequestDTO request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getUserType() != User.UserType.STUDENT) {
            throw new BadRequestException("User is not allowed to have a student profile");
        }

        if (studentRepository.existsByUserId(user.getId())) {
            throw new DuplicateResourceException("Student profile already exists");
        }

        Student student = new Student();
        student.setFirstName(request.getFirstName());
        student.setLastName(request.getLastName());
        student.setGender(request.getGender());
        student.setBirthDate(request.getBirthDate());
        student.setAddress(request.getAddress());
        student.setInterest(request.getInterest());
        student.setEnrollmentDate(LocalDate.now());

        student.setUser(user) ;

        Student savedStudent = studentRepository.save(student);

        return mapToResponse(savedStudent);
    }

    @Transactional(readOnly = true)
    public StudentResponseDTO getStudentById(Long id) {

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        return mapToResponse(student);
    }

    @Transactional(readOnly = true)
    public List<StudentResponseDTO> getAllStudents() {

        return studentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public StudentResponseDTO updateStudent(Long id, StudentRequestDTO request) {

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        student.setFirstName(request.getFirstName());
        student.setLastName(request.getLastName());
        student.setGender(request.getGender());
        student.setBirthDate(request.getBirthDate());
        student.setAddress(request.getAddress());
        student.setInterest(request.getInterest());

        return mapToResponse(student);
    }

    public void deleteStudent(Long id) {

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        studentRepository.delete(student);
    }

    //MAPPER
    private StudentResponseDTO mapToResponse(Student student) {

        return StudentResponseDTO.builder()
                .id(student.getId())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .gender(student.getGender())
                .birthDate(student.getBirthDate())
                .enrollmentDate(student.getEnrollmentDate())
                .address(student.getAddress())
                .interest(student.getInterest())
                .userId(student.getUser().getId())
                .build();
    }
}