package com.sumit.DocQueue.service;

import com.sumit.DocQueue.model.Doctor;
import com.sumit.DocQueue.model.Patient;
import com.sumit.DocQueue.model.QueueEntry;
import com.sumit.DocQueue.model.QueueStatus;
import com.sumit.DocQueue.repository.DoctorRepository;
import com.sumit.DocQueue.repository.PatientRepository;
import com.sumit.DocQueue.repository.QueueEntryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.management.Query;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;



@Service
@RequiredArgsConstructor
public class QueueService {
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final QueueEntryRepository queueEntryRepository;
    private final NotificationService notificationService;
    @Transactional
    public QueueEntry addPatientToQueue(Long doctorId,Long patientId){
        Doctor doctor=doctorRepository.findById(doctorId).
                orElseThrow(()->new RuntimeException("Doctor not found"));
        Patient patient=patientRepository.findById(patientId).
                orElseThrow(()->new RuntimeException("Patient not found"));
        List<QueueEntry> existingEntries=queueEntryRepository.findByDoctor(doctor);
        int nextTokenNumber=existingEntries.size()+1;

        QueueEntry entry=new QueueEntry();
        entry.setDoctor(doctor);
        entry.setPatient(patient);
        entry.setTokenNumber(nextTokenNumber);
        entry.setStatus(QueueStatus.WAITING);

        return queueEntryRepository.save(entry);
    }
    public List<Doctor> getAllAvailableDoctors(){
        List<Doctor> doctors=doctorRepository.findAll();
        if(doctors.isEmpty()){
            System.out.println("No doctors available right now");
        }
        return doctors;
    }
    @Transactional
    public Patient callNextPatient(Long doctorId){
        Doctor doctor=doctorRepository.findById(doctorId).
                orElseThrow(()->new RuntimeException("Doctor not found"));


        Optional<QueueEntry> presentEntryOpt=queueEntryRepository.
                findFirstByDoctorAndStatus(doctor,QueueStatus.IN_CONSULT);
        // change the status of the patient who is in the clinic as completed first
        if(presentEntryOpt.isPresent()){
            QueueEntry entry=presentEntryOpt.get();
            entry.setStatus(QueueStatus.COMPLETED);
            queueEntryRepository.save(entry);
        }
        Optional<QueueEntry> nextEntryOpt=queueEntryRepository.
                findFirstByDoctorAndStatusOrderByTokenNumberAsc(doctor,QueueStatus.WAITING);
        // if someone is waiting change their status to consulting
        if(nextEntryOpt.isPresent()){
            QueueEntry entry=nextEntryOpt.get();
            entry.setStatus(QueueStatus.IN_CONSULT);
            queueEntryRepository.save(entry);
            notificationService.sendUpdate(doctorId,entry);
            return entry.getPatient();
        }

        return null;
    }
    @Transactional
    public Patient skipPatient(Long doctorId){
        // get the doctor
        Doctor doctor=doctorRepository.findById(doctorId).
                orElseThrow(()->new RuntimeException("Doctor not found"));

        // find the patient who was called in
        Optional<QueueEntry> presentEntryOpt=queueEntryRepository.
                findFirstByDoctorAndStatus(doctor,QueueStatus.IN_CONSULT);
        if(presentEntryOpt.isPresent()){
            QueueEntry entry=presentEntryOpt.get();
            entry.setStatus(QueueStatus.PENDING);
            queueEntryRepository.save(entry);
        }

        return callNextPatient(doctorId);
    }

    public void restorePendingQueue(Long doctorId){
        Doctor doctor=doctorRepository.findById(doctorId).
                orElseThrow(()->new RuntimeException("404: Doctor not found"));
        queueEntryRepository.updateStatusForDoctor(doctor,QueueStatus.PENDING, QueueStatus.WAITING);
    }
    public void restorePatient(Long queueEntryId){
        QueueEntry entry=queueEntryRepository.findById(queueEntryId).
                orElseThrow(()->new RuntimeException("404: Patient not found"));
        entry.setStatus(QueueStatus.WAITING);
        queueEntryRepository.save(entry);
    }
    public List<QueueEntry> getTodaysQueue(Long doctorId){
        Doctor doctor=doctorRepository.findById(doctorId).
                orElseThrow(()->new RuntimeException("404: Doctor not found"));

        LocalDateTime start=LocalDate.now().atStartOfDay();
        LocalDateTime end=LocalDate.now().atTime(LocalTime.MAX);
        List<QueueEntry> entry=queueEntryRepository.
                findAllByDoctorAndStatusAndCreatedAtBetweenOrderByTokenNumberAsc(
                            doctor, QueueStatus.WAITING, start, end
                        );
        return entry;
    }


}
