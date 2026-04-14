package com.sumit.DocQueue.model;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "queue_entries")
@Data
public class QueueEntry {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Doctor doctor;

    @ManyToOne
    private Patient patient;

    private Integer tokenNumber;

    @Enumerated(EnumType.STRING)
    private QueueStatus status;

    @Column(updatable = false)
    private LocalDateTime createdAt=LocalDateTime.now();
}
