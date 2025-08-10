package de.his.patient.application.service;

import de.his.patient.application.dto.*;
import de.his.patient.domain.model.Patient;
import de.his.patient.domain.repository.PatientRepository;
import de.his.patient.infrastructure.exception.PatientNotFoundException;
import de.his.patient.infrastructure.exception.PatientAlreadyExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class PatientService {

    private static final Logger logger = LoggerFactory.getLogger(PatientService.class);

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Transactional
    public PatientResponse createPatient(CreatePatientRequest request) {
        logger.info("Creating new patient with KVNR {}", request.getKvnr());

        // Check if patient with KVNR already exists
        if (patientRepository.findByKvnrAndDeletedAtIsNull(request.getKvnr()).isPresent()) {
            throw new PatientAlreadyExistsException(request.getKvnr());
        }

        Patient patient = new Patient(
            request.getFirstName(),
            request.getLastName(),
            request.getBirthDate(),
            request.getGender(),
            request.getKvnr()
        );

        // Set additional fields
        patient.setTitle(request.getTitle());
        patient.setInsuranceNumber(request.getInsuranceNumber());
        patient.setInsuranceType(request.getInsuranceType());
        patient.setInsuranceCompanyId(request.getInsuranceCompanyId());
        patient.setInsuranceCompanyName(request.getInsuranceCompanyName());
        patient.setPhone(request.getPhone());
        patient.setEmail(request.getEmail());
        patient.setConsentCommunication(request.getConsentCommunication());
        patient.setConsentDataProcessing(request.getConsentDataProcessing());

        patient = patientRepository.save(patient);
        
        logger.info("Created patient {} with KVNR {}", patient.getId(), request.getKvnr());

        return mapToResponse(patient);
    }

    @Transactional(readOnly = true)
    public PatientResponse getPatient(UUID patientId) {
        Patient patient = getPatientEntity(patientId);
        return mapToResponse(patient);
    }

    @Transactional(readOnly = true)
    public PatientResponse getPatientByKvnr(String kvnr) {
        Patient patient = patientRepository.findByKvnrAndDeletedAtIsNull(kvnr)
            .orElseThrow(() -> new PatientNotFoundException("KVNR: " + kvnr));
        return mapToResponse(patient);
    }

    @Transactional(readOnly = true)
    public Page<PatientSummary> searchPatients(String searchTerm, Pageable pageable) {
        return patientRepository.searchPatients(searchTerm, pageable)
            .map(this::mapToSummary);
    }

    @Transactional
    public void deletePatient(UUID patientId) {
        Patient patient = getPatientEntity(patientId);
        patient.markAsDeleted();
        
        patientRepository.save(patient);
        logger.info("Soft deleted patient {}", patientId);
    }

    private Patient getPatientEntity(UUID patientId) {
        return patientRepository.findById(patientId)
            .filter(patient -> !patient.isDeleted())
            .orElseThrow(() -> new PatientNotFoundException(patientId.toString()));
    }

    private PatientResponse mapToResponse(Patient patient) {
        return new PatientResponse(
            patient.getId(),
            patient.getFirstName(),
            patient.getLastName(),
            patient.getTitle(),
            patient.getBirthDate(),
            patient.getGender(),
            patient.getKvnr(),
            patient.getInsuranceNumber(),
            patient.getInsuranceStatus(),
            patient.getInsuranceType(),
            patient.getInsuranceCompanyId(),
            patient.getInsuranceCompanyName(),
            patient.getPhone(),
            patient.getEmail(),
            patient.getConsentCommunication(),
            patient.getConsentDataProcessing(),
            patient.getCreatedAt(),
            patient.getUpdatedAt()
        );
    }

    private PatientSummary mapToSummary(Patient patient) {
        return new PatientSummary(
            patient.getId(),
            patient.getFullName(),
            patient.getBirthDate(),
            patient.getGender(),
            patient.getKvnr(),
            patient.getInsuranceStatus(),
            patient.getInsuranceCompanyName()
        );
    }
}
