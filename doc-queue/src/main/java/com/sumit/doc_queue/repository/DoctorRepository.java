package com.sumit.doc_queue.repository;

import com.sumit.doc_queue.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
public interface DoctorRepository extends JpaRepository<Doctor,Long> {

}
