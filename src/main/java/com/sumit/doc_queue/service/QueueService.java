package com.sumit.doc_queue.service;

import com.sumit.doc_queue.model.*;
import com.sumit.doc_queue.repository.DoctorRepository;
import com.sumit.doc_queue.repository.PatientRepository;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Duration;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@AllArgsConstructor
public class QueueService {
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorSessionService doctorSessionService;
    private final AuthService authService;
//    private final List<SseEmitter> emitters=new CopyOnWriteArrayList<>(); // thread safe ArrayList
    private final Map<Long, List<SseEmitter>> doctorEmitters = new ConcurrentHashMap<>();

    private void sendQueueState(SseEmitter emitter,List<Patient> waitingQueue,Patient active,String status,Double avgTime) throws Exception{
        emitter.send(SseEmitter.event()
                .name("Queue-Update")
                .data(waitingQueue));
        emitter.send(SseEmitter.event()
                .name("Session-Status")
                .data(status));
        emitter.send(SseEmitter.event()
                .name("Average-Waiting-Time")
                .data(avgTime));
        if(active!=null){
            emitter.send(SseEmitter.event()
                    .name("Active-Patient")
                    .data(active));
        }else{
            emitter.send(SseEmitter.event()
                    .name("Active-Patient")
                    .data("{\"fullName\":\"Nobody\"}"));
        }
    }
    public Patient registerPatient(String name, Doctor doctor){
        if(!doctorSessionService.checkSession(doctor.getId())){
            throw new RuntimeException("Registration is closed!");
        }
        DoctorSession session=doctorSessionService.getTodaySession(doctor.getId());
        Patient p=new Patient();
        p.setFullName(name);
        p.setArrivalTime(java.time.LocalDateTime.now());
        p.setDoctor(doctor); // attach the doctor
        p.setSession(session);

        patientRepository.save(p);
        System.out.println(p.getFullName()+" is saved in the database");
//        this.broadcastQueueSize();
        this.broadcastQueue(doctor.getId());
        return p;
    }

    public Optional<Patient> callNextPatient(Long doctorId){
        authService.validateDoctorOwnership(doctorId);
        DoctorSession session=doctorSessionService.getTodaySession(doctorId);
        if(session.getStatus()==SessionStatus.PAUSED){
            throw new RuntimeException("Cannot call next patient while session is paused. Resume the session first.");
        }
        // time
        LocalTime now=LocalTime.now();
        List<Patient> activePatient=patientRepository.findByDoctorIdAndStatusOrderByArrivalTime(doctorId,QueueStatus.IN_PROGRESS);
        if(!activePatient.isEmpty()){
            Patient currentPatient=activePatient.get(0);
            currentPatient.setStatus(QueueStatus.COMPLETED);
            currentPatient.setOutTime(now);
            if(currentPatient.getInTime()!=null){
                long seconds= Duration.between(currentPatient.getInTime(),now).toSeconds();
                double minutes=seconds/60.0;
                currentPatient.setConsultationDuration(minutes);
                if(minutes>0 && minutes<120){
                    Doctor doctor=currentPatient.getDoctor();
                    Double avgTime=doctor.getConsultationTime()==null? 0: doctor.getConsultationTime();
                    Long count=doctor.getTotalPatients();
                    if(count==null){
                        doctor.setConsultationTime(minutes);
                        doctor.setTotalPatients(1L);
                    }else{
                        doctor.setConsultationTime((avgTime*count+minutes)/(count+1));
                        doctor.setTotalPatients(count+1);
                    }
                    doctorRepository.save(doctor);
                }
            }
            patientRepository.save(currentPatient);
        }
        List<Patient> patientList=patientRepository.findByDoctorIdAndStatusOrderByArrivalTime(doctorId,QueueStatus.WAITING);
        if(patientList.isEmpty()){
            this.broadcastQueue(doctorId);
            return Optional.empty();
        }else{
            Patient p=patientList.get(0);
            // call the patient
            System.out.println(p.getFullName()+" is called for Doctor Id: "+doctorId);
            p.setStatus(QueueStatus.IN_PROGRESS);
            p.setInTime(now);
            patientRepository.save(p);
//            this.broadcastQueueSize();
            this.broadcastQueue(doctorId);
            return Optional.of(p);
        }
    }

    // live connection
    public SseEmitter subscribe(Long doctorId){
        List<SseEmitter> emitters=doctorEmitters.computeIfAbsent(doctorId,k->new CopyOnWriteArrayList<>());
        SseEmitter emitter=new SseEmitter(60*30*1000L);
        emitters.add(emitter);
        emitter.onError((ex)->emitters.remove(emitter));
        emitter.onCompletion(()->emitters.remove(emitter));
        emitter.onTimeout(()->emitters.remove(emitter));
        try{
            List<Patient> waitingQueue=patientRepository.findByDoctorIdAndStatusOrderByArrivalTime(doctorId,QueueStatus.WAITING);
            List<Patient> progressQueue=patientRepository.findByDoctorIdAndStatusOrderByArrivalTime(doctorId,QueueStatus.IN_PROGRESS);
            Patient patient=null;
            DoctorSession session=doctorSessionService.getTodaySession(doctorId);
            Doctor d=doctorRepository.findById(doctorId).orElseThrow(()->new RuntimeException("Doctor not found"));
            if(!progressQueue.isEmpty()){
                patient=progressQueue.get(progressQueue.size()-1);
            }
            sendQueueState(emitter,waitingQueue,patient,session.getStatus().name(),d.getConsultationTime());
        }catch (Exception e){
            emitters.remove(emitter);
        }
        return emitter;
    }

    public void broadcastQueue(Long doctorId){
        List<Patient> waitingQueue=patientRepository.findByDoctorIdAndStatusOrderByArrivalTime(doctorId,QueueStatus.WAITING);
        List<Patient> progressQueue=patientRepository.findByDoctorIdAndStatusOrderByArrivalTime(doctorId,QueueStatus.IN_PROGRESS);
        Patient patient=null;
        List<SseEmitter> emitters=doctorEmitters.get(doctorId);
        DoctorSession session=doctorSessionService.getTodaySession(doctorId);
        Doctor d=doctorRepository.findById(doctorId).orElseThrow(()->new RuntimeException("Doctor not found"));
        if (emitters == null || emitters.isEmpty()) {
            return; // Nobody is currently watching this doctor's stream!
        }
        if(!progressQueue.isEmpty()){
            patient=progressQueue.get(progressQueue.size()-1);
        }
        for(SseEmitter emitter: emitters){
            try{
                sendQueueState(emitter,waitingQueue,patient,session.getStatus().name(),d.getConsultationTime());
            }catch(java.io.IOException e) {
                // This just means a user closed or refreshed their browser tab.
                // We silent-remove them without printing a massive scary red stack trace!
                emitters.remove(emitter);
            }catch(Exception e){
                System.out.println("Unexpected broadcast error: "+e.getMessage());
                emitters.remove(emitter);
            }
        }
    }

    public Doctor find(Long id){
        return doctorRepository.findById(id).
                orElseThrow(()->new RuntimeException("Doctor not found with ID: "+id));
    }

    public void makeCurrentPatientMissed(Long doctorId){
        authService.validateDoctorOwnership(doctorId);
        List<Patient> activePatient=patientRepository.findByDoctorIdAndStatusOrderByArrivalTime(doctorId,QueueStatus.IN_PROGRESS);
        if(!activePatient.isEmpty()){
            activePatient.get(0).setStatus(QueueStatus.MISSED);
            patientRepository.save(activePatient.get(0));
        }
        callNextPatient(doctorId);
    }
    @Scheduled(fixedRate = 25000)
    public void sendHeartBeat(){
        doctorEmitters.forEach((doctorId,emitters)->{
            for(SseEmitter emitter:emitters){
                try{
                    emitter.send(SseEmitter.event().comment("keep-alive"));
                }catch(Exception e){
                    emitters.remove(emitter);
                }
            }
        });
    }
}
