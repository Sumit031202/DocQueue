package com.sumit.doc_queue.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorInfo {
    private Long id;
    private String name;
    private Double consultationTime;
    private LocalTime startTime;
    private LocalTime endTime;
}
