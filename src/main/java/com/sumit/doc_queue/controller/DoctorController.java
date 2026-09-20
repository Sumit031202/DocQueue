package com.sumit.doc_queue.controller;

import com.sumit.doc_queue.dto.DoctorRequest;
import com.sumit.doc_queue.dto.DoctorResponse;
import com.sumit.doc_queue.dto.SessionTimingRequest;
import com.sumit.doc_queue.model.DoctorSession;
import com.sumit.doc_queue.model.Patient;
import com.sumit.doc_queue.service.DoctorService;
import com.sumit.doc_queue.service.DoctorSessionService;
import com.sumit.doc_queue.service.QueueService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/doctors")
@AllArgsConstructor
public class DoctorController {
    private final DoctorService doctorService;
    private final QueueService queueService;
    private final DoctorSessionService doctorSessionService;

    @PostMapping("/{doctorId}/next")
    public Optional<Patient> callNextPatient(@PathVariable Long doctorId){
        return queueService.callNextPatient(doctorId);
    }
    @PatchMapping("/{doctorId}/toggle")
    public DoctorSession toggleStatus(@PathVariable Long doctorId){
        DoctorSession session=doctorSessionService.toggleSessionStatus(doctorId);
        queueService.broadcastQueue(doctorId);
        return session;
    }
    @PatchMapping("/{doctorId}/end")
    public void endSession(@PathVariable Long doctorId){
        doctorSessionService.endTheSession(doctorId);
        queueService.broadcastQueue(doctorId);
    }
    @PutMapping("/{doctorId}/session")
    public DoctorSession updateSession(@PathVariable Long doctorId, @RequestBody SessionTimingRequest timing){
        return doctorSessionService.updateSession(doctorId,timing.getStartTime(),timing.getEndTime());
    }
}
