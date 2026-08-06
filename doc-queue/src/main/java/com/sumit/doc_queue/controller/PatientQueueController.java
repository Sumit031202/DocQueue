package com.sumit.doc_queue.controller;

import com.sumit.doc_queue.model.Doctor;
import com.sumit.doc_queue.model.Patient;
import com.sumit.doc_queue.repository.DoctorRepository;
import com.sumit.doc_queue.service.QueueService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/patients")
public class PatientQueueController {
    private final QueueService queueService;
    private final DoctorRepository doctorRepository;
    public PatientQueueController(QueueService queueService,DoctorRepository doctorRepository){
        this.queueService=queueService;
        this.doctorRepository=doctorRepository;
    }
    @PostMapping("queue/join/{doctorId}/{name}")
    public Patient joinQueue(@PathVariable Long doctorId,@PathVariable String name){
        Doctor doctor=doctorRepository.findById(doctorId)
                .orElseThrow(()->new RuntimeException("Doctor not found with ID: "+doctorId));
        Patient patient=queueService.registerPatient(name,doctor);
        return patient;
    }

    @GetMapping(value = "/stream",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamUpdates(@RequestParam Long doctorId){
        return queueService.subscribe(doctorId);
    }

}
