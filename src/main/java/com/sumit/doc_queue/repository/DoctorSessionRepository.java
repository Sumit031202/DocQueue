package com.sumit.doc_queue.repository;

import com.sumit.doc_queue.model.DoctorSession;
import com.sumit.doc_queue.model.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DoctorSessionRepository extends JpaRepository<DoctorSession,Long> {
    Optional<DoctorSession> findByDoctorIdAndSessionDateAndStatus(Long doctorId, LocalDate sessionDate, SessionStatus status);
}
