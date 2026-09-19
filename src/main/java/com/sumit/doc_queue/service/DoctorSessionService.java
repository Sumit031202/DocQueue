package com.sumit.doc_queue.service;

import com.sumit.doc_queue.model.Doctor;
import com.sumit.doc_queue.model.DoctorSession;
import com.sumit.doc_queue.model.SessionStatus;
import com.sumit.doc_queue.repository.DoctorRepository;
import com.sumit.doc_queue.repository.DoctorSessionRepository;
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
