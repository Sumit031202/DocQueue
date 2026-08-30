package com.sumit.doc_queue.controller;

import com.sumit.doc_queue.model.Doctor;
import com.sumit.doc_queue.model.Patient;
import com.sumit.doc_queue.repository.DoctorRepository;
import com.sumit.doc_queue.service.QueueService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/doctors")
public class DoctorController {
    private final DoctorRepository doctorRepository;
    private final QueueService queueService;
    public DoctorController(DoctorRepository doctorRepository,QueueService queueService){
        this.doctorRepository=doctorRepository;
        this.queueService=queueService;
    }

    @PostMapping
    public Doctor addDoctor(@RequestBody Doctor doctor){
        doctorRepository.save(doctor);
        return doctor;
    }
    @GetMapping
    public List<Doctor> getDoctors(){
        List<Doctor> doctors=doctorRepository.findAll();
        return doctors;
    }
    @PostMapping("/{doctorId}/next")
    public Optional<Patient> callNextPatient(@PathVariable Long doctorId){
        return queueService.callNextPatient(doctorId);
    }

}
