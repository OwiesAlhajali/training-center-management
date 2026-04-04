package com.trainingcenter.management.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "grades",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"student_id", "quiz_id"})
        }
)
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Score is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "Score must be at least 0")
    @Column(name = "score", nullable = false, precision = 5, scale = 2)
    private BigDecimal score;

    //many Grade one Student
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    //many Grade one Quiz
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;
}
