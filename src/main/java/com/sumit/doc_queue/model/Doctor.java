package com.sumit.doc_queue.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalTime;

@Data
@Entity
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String specialization;
    private String email;
    private String password;
    @Enumerated(EnumType.STRING)
    private Role role=Role.DOCTOR;

    private Double consultationTime;
    private Long totalPatients;

    private LocalTime defaultStartTime=LocalTime.of(9,0);
    private LocalTime defaultEndTime=LocalTime.of(23,59);
}
