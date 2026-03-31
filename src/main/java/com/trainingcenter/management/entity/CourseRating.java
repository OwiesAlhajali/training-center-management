package com.trainingcenter.management.entity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table (name="Course-Rating", uniqueConstraints =
        {@UniqueConstraint (columnNames = {"course_id","user_id"})
        })

public class CourseRating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id ;

    //Starts 1 To 5
    @Column(nullable = false)
    private BigDecimal rating ;

    // like a comment
    @Column(columnDefinition = "TEXT")
    private String review ;

    // relationship with Courses
    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name ="course_id",nullable = false)
    private Course course ;

    // relationship with Student
    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name ="Student_id",nullable = false)
    private Student student ;
}
