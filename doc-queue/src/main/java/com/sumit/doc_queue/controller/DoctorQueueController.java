package com.sumit.doc_queue.controller;

import com.sumit.doc_queue.model.Patient;
import com.sumit.doc_queue.service.QueueService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/doctors")
@CrossOrigin(origins = {"http://localhost:5173","https://docqueue.online"})
public class DoctorQueueController {
    private final QueueService queueService;
    public DoctorQueueController(QueueService queueService){
        this.queueService=queueService;
    }
    @PutMapping("/next")
    public ResponseEntity<?> callNext(){
        Optional<Patient> nextPatient=queueService.callNextPatient();

        if(nextPatient.isPresent()){
            return ResponseEntity.ok(nextPatient.get());
        }else{
            return ResponseEntity.ok("No patients are currently waiting in the queue");
        }
    }

}