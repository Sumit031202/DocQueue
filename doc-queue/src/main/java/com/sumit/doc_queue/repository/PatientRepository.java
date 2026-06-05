package com.sumit.doc_queue.repository;

import com.sumit.doc_queue.model.Patient;
import com.sumit.doc_queue.model.QueueStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PatientRepository extends JpaRepository<Patient,Long> {
    List<Patient> findByStatusOrderByArrivalTime(QueueStatus status);
}