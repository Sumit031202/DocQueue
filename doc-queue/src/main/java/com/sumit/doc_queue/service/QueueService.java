package com.sumit.doc_queue.service;

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
    public Patient registerPatient(String name){
        Patient p=new Patient();
        p.setFullName(name);
        p.setArrivalTime(java.time.LocalDateTime.now());

        patientRepository.save(p);
        System.out.println(p.getFullName()+" is saved in the database");
        this.broadcastQueueSize();
        return p;
    }

    public Optional<Patient> callNextPatient(){
        List<Patient> patientList=patientRepository.findByStatusOrderByArrivalTime(QueueStatus.WAITING);
        if(patientList.isEmpty()){
            return Optional.empty();
        }else{
            Patient p=patientList.get(0);
            // call the patient
            System.out.println(p.getFullName()+" is called");
            p.setStatus(QueueStatus.IN_PROGRESS);
            patientRepository.save(p);
            this.broadcastQueueSize();
            return Optional.of(p);
        }
    }

    // live connection
    public SseEmitter subscribe(){
        SseEmitter emitter=new SseEmitter(60*30*1000L);
        emitters.add(emitter);
        emitter.onError((ex)->this.emitters.remove(emitter));
        emitter.onCompletion(()->this.emitters.remove(emitter));
        emitter.onTimeout(()->this.emitters.remove(emitter));
        return emitter;
    }

    public void broadcastQueueSize(){
        long waitingCount=patientRepository.findByStatusOrderByArrivalTime(QueueStatus.WAITING).size();
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
}
