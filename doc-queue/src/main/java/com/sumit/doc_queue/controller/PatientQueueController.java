package com.sumit.doc_queue.controller;

import com.sumit.doc_queue.model.Patient;
import com.sumit.doc_queue.service.QueueService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/patients")
@CrossOrigin(origins = {"http://localhost:5173","https://docqueue.online"})
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

    @GetMapping(value = "/stream",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamUpdates(){
        return queueService.subscribe();
    }

}
