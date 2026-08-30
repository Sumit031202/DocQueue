package com.sumit.doc_queue.model;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String fullName;

    private java.time.LocalDateTime arrivalTime;

    @Enumerated(EnumType.STRING) // using this so that we take waiting not 0 as number
    private QueueStatus status=QueueStatus.WAITING;
    @ManyToOne
    @JoinColumn(nullable=false, name="doctor_id")
    private Doctor doctor;
}
