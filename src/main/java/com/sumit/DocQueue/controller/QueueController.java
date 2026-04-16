package com.sumit.DocQueue.controller;

import com.sumit.DocQueue.model.Doctor;
import com.sumit.DocQueue.model.Patient;
import com.sumit.DocQueue.model.QueueEntry;
import com.sumit.DocQueue.repository.DoctorRepository;
import com.sumit.DocQueue.repository.QueueEntryRepository;
import com.sumit.DocQueue.service.NotificationService;
import com.sumit.DocQueue.service.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


import java.util.List;


@RestController
@RequestMapping("/api/queue")
@RequiredArgsConstructor
public class QueueController {
    private final QueueEntryRepository queueEntryRepository;
    private final QueueService queueService;
    private final NotificationService notificationService;
    private final DoctorRepository doctorRepository;

    @GetMapping("/doctors")
    public List<Doctor> getDoctors(){
        return doctorRepository.findAll();
    }

    @GetMapping("/doctor/{doctorId}")
    public List<QueueEntry> getQueueForDoctor(@PathVariable Long doctorId) {
        // 1. Find the doctor object first
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        // 2. Use the custom repository method we wrote earlier
        return queueEntryRepository.findByDoctor(doctor);
    }

    // join the queue
    @PostMapping("/join")
    public QueueEntry joinQueue(@RequestParam Long doctorId,@RequestParam Long patientId){
        return queueService.addPatientToQueue(doctorId,patientId);
    }

    // setting up the SSE emitter connection (one directional: radio)
    @GetMapping("/queue-update/{doctorId}")
    public SseEmitter subscribeToUpdates(@PathVariable Long doctorId){
        return notificationService.addSubscription(doctorId);
    }

    @PostMapping("/next/{doctorId}")
    public Patient callNext(@PathVariable Long doctorId){
        Patient nextPatient=queueService.callNextPatient(doctorId);
        if(nextPatient!=null){
            notificationService.sendUpdate(doctorId,nextPatient);
        }
        return nextPatient;
    }


}
