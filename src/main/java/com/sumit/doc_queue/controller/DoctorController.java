package com.sumit.doc_queue.controller;

import com.sumit.doc_queue.dto.DoctorRequest;
import com.sumit.doc_queue.dto.DoctorResponse;
import com.sumit.doc_queue.model.Patient;
import com.sumit.doc_queue.service.DoctorService;
import com.sumit.doc_queue.service.QueueService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/doctors")
@AllArgsConstructor
public class DoctorController {
    private final DoctorService doctorService;
    private final QueueService queueService;

    @PostMapping
    public DoctorResponse addDoctor(@Valid @RequestBody DoctorRequest doctor){
        return doctorService.save(doctor);
    }
    @GetMapping
    public List<DoctorResponse> getDoctors(){
        return doctorService.getAllDoctors();
    }
    @PostMapping("/{doctorId}/next")
    public Optional<Patient> callNextPatient(@PathVariable Long doctorId){
        return queueService.callNextPatient(doctorId);
    }

}
