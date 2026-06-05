package com.sumit.doc_queue.controller;

import com.sumit.doc_queue.model.Patient;
import com.sumit.doc_queue.service.QueueService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patients")
public class PatientQueueController {
    private final QueueService queueService;
    public PatientQueueController(QueueService queueService){
        this.queueService=queueService;
    }
    @PostMapping("queue/join/{name}")
    public Patient joinQueue(@PathVariable String name){
        Patient patient=queueService.registerPatient(name);
        return patient;
    }

}
