package com.sumit.doc_queue.controller;

import com.sumit.doc_queue.model.Doctor;
import com.sumit.doc_queue.model.Patient;
import com.sumit.doc_queue.repository.DoctorRepository;
import com.sumit.doc_queue.service.DoctorService;
import com.sumit.doc_queue.service.QueueService;
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
    public Doctor addDoctor(@RequestBody Doctor doctor){
        doctorService.save(doctor);
        return doctor;
    }
    @GetMapping
    public List<Doctor> getDoctors(){
        List<Doctor> doctors=doctorService.getAllDoctors();
        return doctors;
    }
    @PostMapping("/{doctorId}/next")
    public Optional<Patient> callNextPatient(@PathVariable Long doctorId){
        return queueService.callNextPatient(doctorId);
    }

}
