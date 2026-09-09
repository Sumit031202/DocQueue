package com.sumit.doc_queue.controller;

import com.sumit.doc_queue.dto.DoctorLoginRequest;
import com.sumit.doc_queue.dto.DoctorRegistrationRequest;
import com.sumit.doc_queue.dto.DoctorResponse;
import com.sumit.doc_queue.service.AuthService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    @PostMapping("/register")
    public DoctorResponse registerDoctor(@Valid @RequestBody DoctorRegistrationRequest doctorRegistrationRequest){
        return authService.register(doctorRegistrationRequest);
    }
    @PostMapping("/login")
    public String login(@Valid @RequestBody DoctorLoginRequest doctorLoginRequest){
        return authService.login(doctorLoginRequest);
    }
}
