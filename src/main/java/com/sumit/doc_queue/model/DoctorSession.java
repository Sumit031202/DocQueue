package com.sumit.doc_queue.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Entity
public class DoctorSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sessionId;
    private LocalDate sessionDate;
    private LocalTime startTime;
    private LocalTime endTime;
    @Enumerated(EnumType.STRING)
    private SessionStatus status;
    @ManyToOne
    @JoinColumn(nullable=false, name="doctor_id")
    private Doctor doctor;
}
