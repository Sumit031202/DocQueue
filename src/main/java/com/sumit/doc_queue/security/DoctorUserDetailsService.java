package com.sumit.doc_queue.security;

import com.sumit.doc_queue.model.Doctor;
import com.sumit.doc_queue.repository.DoctorRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DoctorUserDetailsService implements UserDetailsService {
    private final DoctorRepository doctorRepository;
    @Override
    public DoctorUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Doctor doctor=doctorRepository.findByEmail(username)
                .orElseThrow(()->new UsernameNotFoundException("Doctor not found"));
        return new DoctorUserDetails(doctor);
    }
}
