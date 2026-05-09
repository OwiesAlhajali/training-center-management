package com.trainingcenter.management.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

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

    @NotBlank(message = "Institute name is required")
    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotBlank(message = "Location is required")
    @Column(name = "address", nullable = false)
    private String location;

    @Pattern(regexp = "^\\+?[0-9 .-]{7,20}$", message = "Invalid phone number format")
    @Column(name = "phone_number")
    private String phoneNumber;

    @Email(message = "Invalid email format")
    @Column
    private String email;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "institute_working_days", joinColumns = @JoinColumn(name = "institute_id"))
    @Column(name = "working_day")
    private List<@NotBlank String> workingDays = new ArrayList<>();

    @NotNull(message = "Start time is required")
    @Column(name = "start_time")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    @Column(name = "end_time")
    private LocalTime endTime;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InstituteStatus status = InstituteStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;

    @AssertTrue(message = "End time must be after start time")
    private boolean isValidTimeRange() {
        if (startTime == null || endTime == null) return true;
        return endTime.isAfter(startTime);
    }
}
