package com.trainingcenter.management.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tenants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

   @Column(name = "`key`", unique = true, nullable = false) 
    private String key; // identifire (example: TC-001)

    @Column(nullable = false)
    private String name;

    private String address;
}
