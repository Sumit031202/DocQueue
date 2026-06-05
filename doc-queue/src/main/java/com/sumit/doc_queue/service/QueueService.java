package com.sumit.doc_queue.service;

import com.sumit.doc_queue.model.Patient;
import com.sumit.doc_queue.model.QueueStatus;
import com.sumit.doc_queue.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class QueueService {
    private final PatientRepository patientRepository;
    public QueueService(PatientRepository patientRepository){
        this.patientRepository=patientRepository;
    }
    public Patient registerPatient(String name){
        Patient p=new Patient();
        p.setFullName(name);
        p.setArrivalTime(java.time.LocalDateTime.now());

        patientRepository.save(p);
        System.out.println(p.getFullName()+" is saved in the database");
        return p;
    }

    public Optional<Patient> callNextPatient(){
        List<Patient> patientList=patientRepository.findByStatusOrderByArrivalTime(QueueStatus.WAITING);
        if(patientList.isEmpty()){
            return Optional.empty();
        }else{
            Patient p=patientList.get(0);
            // call the patient
            p.setStatus(QueueStatus.IN_PROGRESS);
            patientRepository.save(p);
            return Optional.of(p);
        }
    }
}
