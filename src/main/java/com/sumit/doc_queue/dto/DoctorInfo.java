package com.sumit.doc_queue.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorInfo {
    private Long id;
    private String name;
    private Double consultationTime;
}
