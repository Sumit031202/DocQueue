package com.sumit.DocQueue.repository;


import com.sumit.DocQueue.model.QueueEntry;
import com.sumit.DocQueue.model.QueueStatus;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.sumit.DocQueue.model.Doctor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface QueueEntryRepository extends JpaRepository<QueueEntry,Long> {
    List<QueueEntry> findByDoctor(Doctor doctor);
    Optional<QueueEntry> findFirstByDoctorAndStatusOrderByTokenNumberAsc(Doctor doctor, QueueStatus status);
    Optional<QueueEntry> findFirstByDoctorAndStatus(Doctor doctor, QueueStatus status);

    // reset queue
    @Modifying
    @Transactional
    @Query("update QueueEntry q set q.status = :newStatus where q.doctor = :doctor and q.status = :oldStatus")
    void updateStatusForDoctor(Doctor doctor, QueueStatus oldStatus,QueueStatus newStatus);

    List<QueueEntry> findAllByDoctorAndStatusAndCreatedAtBetweenOrderByTokenNumberAsc(
            Doctor doctor,
            QueueStatus status,
            LocalDateTime start,
            LocalDateTime end
    );


}
