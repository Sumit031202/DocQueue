package com.sumit.doc_queue.model;

import jakarta.persistence.*;
import lombok.Data;

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
}
