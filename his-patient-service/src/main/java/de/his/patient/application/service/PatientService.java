package de.his.patient.application.service;

import de.his.patient.application.dto.*;
import de.his.patient.domain.model.Address;
import de.his.patient.domain.model.Patient;
import de.his.patient.domain.repository.PatientRepository;
import de.his.patient.domain.repository.AddressRepository;
import de.his.patient.infrastructure.exception.PatientNotFoundException;
import de.his.patient.infrastructure.exception.PatientAlreadyExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.List;

@Service
@Transactional
public class PatientService {

    private static final Logger logger = LoggerFactory.getLogger(PatientService.class);

    private final PatientRepository patientRepository;
    private final AddressRepository addressRepository;

    public PatientService(PatientRepository patientRepository,
                          AddressRepository addressRepository) {
        this.patientRepository = patientRepository;
        this.addressRepository = addressRepository;
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

        if (request.getAddresses() != null) {
            for (CreateAddressRequest addressRequest : request.getAddresses()) {
                Address address = new Address();
                address.setAddressType(addressRequest.getAddressType());
                address.setStreet(addressRequest.getStreet());
                address.setHouseNumber(addressRequest.getHouseNumber());
                address.setPostalCode(addressRequest.getPostalCode());
                address.setCity(addressRequest.getCity());
                address.setState(addressRequest.getState());
                address.setCountry(addressRequest.getCountry());
                patient.addAddress(address);
            }
        }

        patient = patientRepository.save(patient);
        if (!patient.getAddresses().isEmpty()) {
            patient.getAddresses().forEach(address -> address.setPerson(patient));
            addressRepository.saveAll(patient.getAddresses());
        }
        
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
        List<AddressResponse> addresses = patient.getAddresses().stream()
            .map(addr -> new AddressResponse(
                addr.getId(),
                addr.getAddressType(),
                addr.getStreet(),
                addr.getHouseNumber(),
                addr.getPostalCode(),
                addr.getCity(),
                addr.getState(),
                addr.getCountry()))
            .collect(java.util.stream.Collectors.toList());

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
            addresses,
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
