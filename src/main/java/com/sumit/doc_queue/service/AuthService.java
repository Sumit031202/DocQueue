package com.sumit.doc_queue.service;

import com.sumit.doc_queue.dto.DoctorLoginRequest;
import com.sumit.doc_queue.dto.DoctorRegistrationRequest;
import com.sumit.doc_queue.dto.DoctorResponse;
import com.sumit.doc_queue.dto.LoginResponse;
import com.sumit.doc_queue.model.Doctor;
import com.sumit.doc_queue.model.Role;
import com.sumit.doc_queue.repository.DoctorRepository;
import com.sumit.doc_queue.security.DoctorUserDetails;
import com.sumit.doc_queue.security.JwtService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@AllArgsConstructor
public class AuthService {
    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager manager;
    private final JwtService jwtService;

    public DoctorResponse register(DoctorRegistrationRequest doctor){
        if(doctorRepository.existsByEmail(doctor.getEmail())){
            throw new RuntimeException("Doctor with this email id already exists");
        }else{
            Doctor newDoctor=new Doctor();
            newDoctor.setName(doctor.getName());
            newDoctor.setEmail(doctor.getEmail());
            newDoctor.setSpecialization(doctor.getSpecialization());
            newDoctor.setPassword(passwordEncoder.encode(doctor.getPassword()));
            newDoctor.setRole(Role.DOCTOR);
            Doctor savedDoctor=doctorRepository.save(newDoctor);
            return new DoctorResponse(savedDoctor.getId(),savedDoctor.getName(),savedDoctor.getSpecialization());
        }
    }
    public LoginResponse login(DoctorLoginRequest doctorLoginRequest){
        Authentication authentication=new UsernamePasswordAuthenticationToken(doctorLoginRequest.getEmail(),doctorLoginRequest.getPassword());
        Authentication authenticated=manager.authenticate(authentication);
        String email=authenticated.getName();
        Doctor d=doctorRepository.findByEmail(email).orElseThrow();
        return new LoginResponse(d.getId(),jwtService.generateToken(email));
    }
    public void validateDoctorOwnership(Long doctorId){
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        if(authentication==null || !(authentication.getPrincipal() instanceof DoctorUserDetails userDetails)){
            throw new AccessDeniedException("User is not authenticated as a Doctor");
        }
        Long authenticatedDoctorId=userDetails.getDoctorId();

        if(!Objects.equals(authenticatedDoctorId, doctorId)){
            throw new AccessDeniedException("You cannot access another doctor's resources");
        }
    }
}
