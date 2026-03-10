package com.trainingcenter.management.entity;

import jakarta.persistence.*;
import lombok.Data;
@Table(name ="category")
@Entity
@Data
public class Category {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    @Column(unique = true ,nullable = false )
    private String name ;
}
