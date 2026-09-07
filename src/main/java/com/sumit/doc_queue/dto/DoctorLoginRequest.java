package com.sumit.doc_queue.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DoctorLoginRequest {
    @NotBlank
    @Email
    private String email;
    @NotBlank
    private String password;
}
