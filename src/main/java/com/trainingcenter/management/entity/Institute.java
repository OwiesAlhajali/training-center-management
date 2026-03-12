package com.trainingcenter.management.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "institutes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Institute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "working_hours")
    private String workingHours;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String location;


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
}

