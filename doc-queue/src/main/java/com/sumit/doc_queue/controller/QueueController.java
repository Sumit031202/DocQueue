package com.sumit.doc_queue.controller;

import com.sumit.doc_queue.model.Patient;
import com.sumit.doc_queue.repository.PatientRepository;
import com.sumit.doc_queue.service.QueueService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class QueueController {
    private final QueueService queueService;
    public QueueController(QueueService queueService){
        this.queueService=queueService;
    }
    @PostMapping("queue/join/{name}")
    public Patient joinQueue(@PathVariable String name){
        Patient patient=queueService.registerPatient(name);
        return patient;
    }
}
