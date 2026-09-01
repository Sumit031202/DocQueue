package com.sumit.doc_queue.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DoctorRequest {
    @NotBlank
    @Size(max = 50)
    private String name;
    @NotBlank
    @Size(max=25)
    private String specialization;
}
