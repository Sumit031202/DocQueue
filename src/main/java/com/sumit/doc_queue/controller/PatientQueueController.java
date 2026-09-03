package com.sumit.doc_queue.controller;

import com.sumit.doc_queue.model.Doctor;
import com.sumit.doc_queue.model.Patient;
import com.sumit.doc_queue.service.QueueService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@AllArgsConstructor
@RequestMapping("/api/patients")
public class PatientQueueController {
    private final QueueService queueService;
    @PostMapping("queue/join/{doctorId}/{name}")
    public Patient joinQueue(@PathVariable Long doctorId,@PathVariable String name){
        Doctor doctor= queueService.find(doctorId);
        Patient patient=queueService.registerPatient(name,doctor);
        return patient;
    }

    @GetMapping(value = "/stream",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamUpdates(@RequestParam Long doctorId){
        return queueService.subscribe(doctorId);
    }

}
