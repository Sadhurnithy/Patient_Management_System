package com.pm.patientservice.service;

import com.pm.patientservice.dto.PatientRequestDTO;
import com.pm.patientservice.dto.PatientResponseDTO;
import com.pm.patientservice.exception.EmailAlreadyExistsException;
import com.pm.patientservice.exception.PatientNotFoundException;
import com.pm.patientservice.mapper.PatientMapper;
import com.pm.patientservice.model.Patient;
import com.pm.patientservice.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class PatientService {
    //Object
    private final PatientRepository patientRepositoryObj;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepositoryObj = patientRepository;
    }

    //READ
    public List<PatientResponseDTO> getPatients() {
        List<Patient> patients = patientRepositoryObj.findAll();

        List<PatientResponseDTO> patientResponseDTOs =
                patients.stream().map(
                        patient -> PatientMapper.patientToPatientResponseDTO(patient)
                ).toList();
        return patientResponseDTOs;
    }

    //CREATE
    public PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO) {
        //Business Logics
        if (patientRepositoryObj.existsByEmail(patientRequestDTO.getEmail())) {
            throw new EmailAlreadyExistsException(
                    "A patient with this email already exists " + patientRequestDTO.getEmail());
        }

        Patient newPatient = patientRepositoryObj.save(
                PatientMapper.patientResponseDTOToPatient(patientRequestDTO)
        );
        return PatientMapper.patientToPatientResponseDTO(newPatient);
    }


    //UPDATE
    public PatientResponseDTO updatePatient(
            UUID id, PatientRequestDTO patientRequestDTO
    ) {

        Patient patient = patientRepositoryObj.findById(id).orElseThrow(
                () -> new PatientNotFoundException("Patient not found with ID: " + id));

        if (patientRepositoryObj.existsByEmailAndIdNot(patientRequestDTO.getEmail(), id)) {
            throw new EmailAlreadyExistsException(
                    "A patient with this email already exists " + patientRequestDTO.getEmail());
        }

        patient.setName(patientRequestDTO.getName());
        patient.setAddress(patientRequestDTO.getAddress());
        patient.setEmail(patientRequestDTO.getEmail());
        patient.setDateOfBirth(LocalDate.parse(patientRequestDTO.getDateOfBirth()));

        Patient updatedPatient = patientRepositoryObj.save(patient);
        return PatientMapper.patientToPatientResponseDTO(updatedPatient);
    }

    //DELETE
    public void deletePatient(UUID id) {
        Patient patient = patientRepositoryObj.findById(id).orElseThrow(
                () -> new PatientNotFoundException("Patient not found with ID: " + id)
        );
        patientRepositoryObj.delete(patient);
    }
}
