package com.sumit.doc_queue.controller;

import com.sumit.doc_queue.dto.DoctorInfo;
import com.sumit.doc_queue.dto.DoctorRequest;
import com.sumit.doc_queue.dto.DoctorResponse;
import com.sumit.doc_queue.model.DoctorSession;
import com.sumit.doc_queue.service.DoctorService;
import com.sumit.doc_queue.service.DoctorSessionService;
import com.sumit.doc_queue.service.QueueService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/public")
@AllArgsConstructor
public class PublicController {
    private final QueueService queueService;
    private final DoctorService doctorService;
    private final DoctorSessionService doctorSessionService;
    @GetMapping("/doctors")
    public List<DoctorResponse> getDoctors(){
        return doctorService.getAllDoctors();
    }
    @PostMapping("/doctors")
    public DoctorResponse addDoctor(@Valid @RequestBody DoctorRequest doctor){
        return doctorService.save(doctor);
    }
    @GetMapping(value = "/stream/{doctorId}",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamUpdates(@PathVariable Long doctorId){
        return queueService.subscribe(doctorId);
    }
    @GetMapping("/{doctorId}/session")
    public DoctorSession getSession(@PathVariable Long doctorId){
        return doctorSessionService.getOrCreateTodaySession(doctorId);
    }

    @GetMapping("/{doctorId}")
    public DoctorInfo getInfo(@PathVariable Long doctorId){
        return doctorService.getInfo(doctorId);
    }

    @GetMapping("/{doctorId}/check")
    public boolean checkSession(@PathVariable Long doctorId){
        return doctorSessionService.checkSession(doctorId);
    }
}
