package com.sumit.doc_queue.service;

import com.sumit.doc_queue.model.Doctor;
import com.sumit.doc_queue.model.Patient;
import com.sumit.doc_queue.model.QueueStatus;
import com.sumit.doc_queue.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class QueueService {
    private final PatientRepository patientRepository;
    private final List<SseEmitter> emitters=new CopyOnWriteArrayList<>(); // thread safe ArrayList
    public QueueService(PatientRepository patientRepository){
        this.patientRepository=patientRepository;
    }
    public Patient registerPatient(String name, Doctor doctor){
        Patient p=new Patient();
        p.setFullName(name);
        p.setArrivalTime(java.time.LocalDateTime.now());
        p.setDoctor(doctor); // attach the doctor

        patientRepository.save(p);
        System.out.println(p.getFullName()+" is saved in the database");
//        this.broadcastQueueSize();
        this.broadcastQueue(doctor.getId());
        return p;
    }

    public Optional<Patient> callNextPatient(Long doctorId){
        List<Patient> activePatient=patientRepository.findByDoctorIdAndStatusOrderByArrivalTime(doctorId,QueueStatus.IN_PROGRESS);
        if(!activePatient.isEmpty()){
            Patient currentPatient=activePatient.get(0);
            currentPatient.setStatus(QueueStatus.COMPLETED);
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
            patientRepository.save(p);
//            this.broadcastQueueSize();
            this.broadcastQueue(doctorId);
            return Optional.of(p);
        }
    }

    // live connection
    public SseEmitter subscribe(Long doctorId){
        SseEmitter emitter=new SseEmitter(60*30*1000L);
        emitters.add(emitter);
        emitter.onError((ex)->this.emitters.remove(emitter));
        emitter.onCompletion(()->this.emitters.remove(emitter));
        emitter.onTimeout(()->this.emitters.remove(emitter));
        try{
            List<Patient> waitingQueue=patientRepository.findByDoctorIdAndStatusOrderByArrivalTime(doctorId,QueueStatus.WAITING);
            List<Patient> progressQueue=patientRepository.findByDoctorIdAndStatusOrderByArrivalTime(doctorId,QueueStatus.IN_PROGRESS);
            Patient patient=null;
            if(!progressQueue.isEmpty()){
                patient=progressQueue.get(progressQueue.size()-1);
            }
            emitter.send(SseEmitter.event()
                    .name("Queue-Update")
                    .data(waitingQueue));
            if(patient!=null){
                emitter.send(SseEmitter.event()
                        .name("Active-Patient")
                        .data(patient));
            }else{
                emitter.send(SseEmitter.event()
                        .name("Active-Patient")
                        .data("{\"fullName\":\"Nobody\"}"));
            }
        }catch (Exception e){
            this.emitters.remove(emitter);
        }
        return emitter;
    }

    public void broadcastQueueSize(Long doctorId){
        long waitingCount=patientRepository.findByDoctorIdAndStatusOrderByArrivalTime(doctorId,QueueStatus.WAITING).size();
        for(SseEmitter emitter: emitters){
            try{
                emitter.send(SseEmitter.event()
                        .name("Queue-Update")
                        .data(waitingCount));
            }catch(Exception e){
                emitters.remove(emitter);
            }
        }
    }
    public void broadcastQueue(Long doctorId){
        List<Patient> waitingQueue=patientRepository.findByDoctorIdAndStatusOrderByArrivalTime(doctorId,QueueStatus.WAITING);
        List<Patient> progressQueue=patientRepository.findByDoctorIdAndStatusOrderByArrivalTime(doctorId,QueueStatus.IN_PROGRESS);
        Patient patient=null;
        if(!progressQueue.isEmpty()){
            patient=progressQueue.get(progressQueue.size()-1);
        }
        for(SseEmitter emitter: emitters){
            try{
                emitter.send(SseEmitter.event()
                        .name("Queue-Update")
                        .data(waitingQueue));
                if(patient!=null){
                    emitter.send(SseEmitter.event()
                            .name("Active-Patient")
                            .data(patient));
                }else{
                    emitter.send(SseEmitter.event()
                            .name("Active-Patient")
                            .data("{\"fullName\":\"Nobody\"}"));
                }

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
}
