package com.sumit.doc_queue.service;

import com.sumit.doc_queue.dto.DoctorRequest;
import com.sumit.doc_queue.dto.DoctorResponse;
import com.sumit.doc_queue.model.Doctor;
import com.sumit.doc_queue.repository.DoctorRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class DoctorService {
    private final DoctorRepository doctorRepository;
    public DoctorResponse save(DoctorRequest doctor) {
        Doctor d=new Doctor();
        d.setName(doctor.getName());
        d.setSpecialization(doctor.getSpecialization());
        Doctor savedDoctor=doctorRepository.save(d);
        return new DoctorResponse(savedDoctor.getId(),savedDoctor.getName(),savedDoctor.getSpecialization());
    }

    public List<DoctorResponse> getAllDoctors() {
        List<Doctor> doctors=doctorRepository.findAll();
        List<DoctorResponse> doctorResponses=new ArrayList<>();
        for(Doctor d: doctors){
            doctorResponses.add(new DoctorResponse(d.getId(),d.getName(),d.getSpecialization()));
        }
        return doctorResponses;
    }
}
