package com.sumit.doc_queue.service;

import com.sumit.doc_queue.model.*;
import com.sumit.doc_queue.repository.DoctorRepository;
import com.sumit.doc_queue.repository.DoctorSessionRepository;
import com.sumit.doc_queue.repository.PatientRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class DoctorSessionService {
    private final DoctorSessionRepository doctorSessionRepository;
    private final DoctorRepository doctorRepository;
    private final AuthService authService;
    private final PatientRepository patientRepository;
    public DoctorSession toggleSessionStatus(Long doctorId){
        authService.validateDoctorOwnership(doctorId);
        LocalDate today=LocalDate.now();
        DoctorSession existingSession=doctorSessionRepository.findByDoctorIdAndSessionDateAndStatusIn(doctorId,today,List.of(SessionStatus.ACTIVE,SessionStatus.PAUSED)).orElseThrow(()->new RuntimeException("No session found with this doctorId: "+doctorId));
        if(existingSession.getStatus()==SessionStatus.ACTIVE){
            existingSession.setStatus(SessionStatus.PAUSED);
        }else{
            existingSession.setStatus(SessionStatus.ACTIVE);
        }
        return doctorSessionRepository.save(existingSession);
    }
    public DoctorSession getOrCreateTodaySession(Long doctorId){
        LocalDate today=LocalDate.now();
        Optional<DoctorSession> existingSession=doctorSessionRepository.findByDoctorIdAndSessionDateAndStatusIn(doctorId, LocalDate.now(), List.of(SessionStatus.ACTIVE, SessionStatus.PAUSED));
        if(existingSession.isPresent()){
            return existingSession.get();
        }
        DoctorSession newSession=new DoctorSession();
        Doctor doctor=doctorRepository.findById(doctorId)
                .orElseThrow(()->new RuntimeException("Doctor not found with id: "+doctorId));
        newSession.setSessionDate(today);
        newSession.setDoctor(doctor);
        newSession.setStartTime(doctor.getDefaultStartTime());
        newSession.setEndTime(doctor.getDefaultEndTime());
        newSession.setStatus(SessionStatus.ACTIVE);
        return doctorSessionRepository.save(newSession);
    }
    public void endTheSession(Long doctorId){
        authService.validateDoctorOwnership(doctorId);
        LocalDate today=LocalDate.now();
        DoctorSession existingSession=doctorSessionRepository.findByDoctorIdAndSessionDateAndStatusIn(doctorId,today,List.of(SessionStatus.ACTIVE,SessionStatus.PAUSED)).orElseThrow(()->new RuntimeException("No active sessions for this doctor"));
        existingSession.setStatus(SessionStatus.COMPLETED);
        doctorSessionRepository.save(existingSession);
        LocalTime now=LocalTime.now();
        List<Patient> activePatient=patientRepository.findByDoctorIdAndStatusOrderByArrivalTime(doctorId, QueueStatus.IN_PROGRESS);
        if(!activePatient.isEmpty()){
            activePatient.get(0).setOutTime(now);
            activePatient.get(0).setStatus(QueueStatus.COMPLETED);
            patientRepository.save(activePatient.get(0));
        }
        List<Patient> waitingPatients=patientRepository.findByDoctorIdAndStatusOrderByArrivalTime(doctorId,QueueStatus.WAITING);
        if(!waitingPatients.isEmpty()){
            for(Patient patient: waitingPatients){
                patient.setStatus(QueueStatus.MISSED);
            }
            patientRepository.saveAll(waitingPatients);
        }
    }
    public DoctorSession updateSession(Long doctorId,LocalTime startTime, LocalTime endTime){
        authService.validateDoctorOwnership(doctorId);
        LocalDate today=LocalDate.now();
        DoctorSession existingSession=doctorSessionRepository.findByDoctorIdAndSessionDateAndStatusIn(doctorId,today,List.of(SessionStatus.ACTIVE,SessionStatus.PAUSED)).orElseThrow(()->new RuntimeException("No active sessions for this doctor"));
        existingSession.setStartTime(startTime);
        existingSession.setEndTime(endTime);
        return doctorSessionRepository.save(existingSession);
    }
    public boolean checkSession(Long doctorId){
        LocalDate today=LocalDate.now();
        Optional<DoctorSession> existingSession=doctorSessionRepository.findByDoctorIdAndSessionDateAndStatusIn(doctorId,today,List.of(SessionStatus.ACTIVE,SessionStatus.PAUSED));
        if(existingSession.isEmpty()){
            return false;
        }
        LocalTime now=LocalTime.now();
        return !now.isBefore(existingSession.get().getStartTime()) && !now.isAfter(existingSession.get().getEndTime());
    }
}
