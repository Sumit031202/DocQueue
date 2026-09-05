package com.sumit.doc_queue.service;

import com.sumit.doc_queue.dto.DoctorRequest;
import com.sumit.doc_queue.dto.DoctorResponse;
import com.sumit.doc_queue.model.Doctor;
import com.sumit.doc_queue.repository.DoctorRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DoctorServiceTest {
    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private DoctorService doctorService;

    @Test
    @Disabled
    public void shouldGetAllDoctors(){
        Doctor d1 = new Doctor();
        Doctor d2 = new Doctor();

        d1.setId(1L);
        d1.setName("Rahul");
        d1.setSpecialization("Cardiologist");

        d2.setId(2L);
        d2.setName("Amit");
        d2.setSpecialization("Child specialist");

        List<Doctor> doctors=List.of(d1,d2);

        // Arrange
        when(doctorRepository.findAll())
                .thenReturn(doctors);

        // Act
        List<DoctorResponse> result=doctorService.getAllDoctors(); // testing of getAllDoctors

        // Assert
        assertEquals(2,result.size());

        assertEquals(1L,result.get(0).getId());
        assertEquals("Rahul",result.get(0).getName());
        assertEquals("Cardiologist",result.get(0).getSpecialization());

        assertEquals(2L,result.get(1).getId());
        assertEquals("Amit",result.get(1).getName());
        assertEquals("Child specialist",result.get(1).getSpecialization());

        // verify
        verify(doctorRepository).findAll();
    }

    @Test
    public void shouldSave(){
        DoctorRequest request = new DoctorRequest("Aditya","Neurologist");

        Doctor d=new Doctor();
        d.setId(1L);
        d.setName(request.getName());
        d.setSpecialization(request.getSpecialization());

        when(doctorRepository.save(any(Doctor.class)))
                .thenReturn(d);

        DoctorResponse response=doctorService.save(request);

        ArgumentCaptor<Doctor> captor=ArgumentCaptor.forClass(Doctor.class);
        verify(doctorRepository).save(captor.capture());
        Doctor savedDoctor=captor.getValue();

        assertEquals("Aditya",savedDoctor.getName());
        assertEquals("Neurologist",savedDoctor.getSpecialization());

        assertEquals(1L, response.getId());
        assertEquals("Aditya", response.getName());
        assertEquals("Neurologist", response.getSpecialization());
    }

}