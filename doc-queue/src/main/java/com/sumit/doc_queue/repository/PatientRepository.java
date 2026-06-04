package com.sumit.doc_queue.repository;

import com.sumit.doc_queue.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient,Long> {

}
