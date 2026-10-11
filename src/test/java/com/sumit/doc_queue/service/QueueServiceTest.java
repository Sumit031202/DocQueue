package com.sumit.doc_queue.service;

import com.sumit.doc_queue.model.Doctor;
import com.sumit.doc_queue.model.DoctorSession;
import com.sumit.doc_queue.model.Patient;
import com.sumit.doc_queue.model.QueueStatus;
import com.sumit.doc_queue.repository.DoctorRepository;
import com.sumit.doc_queue.repository.PatientRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class QueueServiceTest {
    @Mock
    private DoctorSessionService doctorSessionService;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private DoctorRepository doctorRepository;
    @InjectMocks
    private QueueService queueService;

    @Test
    public void shouldRegisterPatient(){
        Doctor doctor=new Doctor();
        doctor.setId(1L);
        String name="Anukul Sharma";
        DoctorSession session=new DoctorSession();
        session.setDoctor(doctor);
        session.setSessionId(2L);

        when(doctorSessionService.checkSession(doctor.getId()))
                .thenReturn(true);
        when(doctorSessionService.getTodaySession(doctor.getId()))
                .thenReturn(session);
        when(patientRepository.findByDoctorIdAndStatusOrderByArrivalTime(doctor.getId(), QueueStatus.WAITING))
                .thenReturn(List.of());
        when(patientRepository.findByDoctorIdAndStatusOrderByArrivalTime(doctor.getId(), QueueStatus.IN_PROGRESS))
                .thenReturn(List.of());
        when(doctorRepository.findById(doctor.getId()))
                .thenReturn(Optional.of(doctor));

        Patient patient=queueService.registerPatient(name,doctor);

        assertEquals("Anukul Sharma",patient.getFullName());
        verify(doctorSessionService).checkSession(doctor.getId());
        verify(doctorSessionService,times(2)).getTodaySession(doctor.getId());
        verify(patientRepository).save(patient);
    }
    @Test
    public void shouldNotRegisterWhenSessionClosed(){
        Doctor doctor=new Doctor();
        doctor.setId(2L);
        String name="Sahadev";

        when(doctorSessionService.checkSession(doctor.getId()))
                .thenReturn(false);

        RuntimeException exception=assertThrows(RuntimeException.class,()->{
            queueService.registerPatient(name,doctor);
        });
        assertEquals("Registration is closed!",exception.getMessage());

        verify(doctorSessionService).checkSession(doctor.getId());
        verify(patientRepository,never()).save(any(Patient.class));
    }
}
