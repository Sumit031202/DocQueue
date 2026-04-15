package com.sumit.DocQueue.repository;

import com.sumit.DocQueue.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor,Long> {
    Optional<Doctor> findByName(String name);
    boolean existsByName(String name);
}
