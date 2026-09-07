package com.sumit.doc_queue.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DoctorRegistrationRequest {
    @NotBlank
    @Size(max=50)
    private String name;
    @NotBlank
    @Size(max=25)
    private String specialization;
    @NotBlank
    @Email
    private String email;
    @NotBlank
    @Size(min=8, max=50)
    private String password;
}
