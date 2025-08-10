// =========================================================================
// Erweiterte EncounterService.java mit verbessertem Exception Handling
// =========================================================================
package de.his.encounter.application.service;

import de.his.encounter.application.dto.CreateEncounterRequest;
import de.his.encounter.application.dto.EncounterResponse;
import de.his.encounter.application.dto.EncounterSummary;
import de.his.encounter.domain.model.Encounter;
import de.his.encounter.domain.model.EncounterDocumentation;
import de.his.encounter.domain.model.EncounterStatus;
import de.his.encounter.domain.repository.EncounterRepository;
import de.his.encounter.infrastructure.exception.*;
import de.his.encounter.infrastructure.service.PatientValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class EncounterService {

    private static final Logger logger = LoggerFactory.getLogger(EncounterService.class);

    private final EncounterRepository encounterRepository;
    private final PatientValidationService patientValidationService;

    public EncounterService(EncounterRepository encounterRepository,
            PatientValidationService patientValidationService) {
        this.encounterRepository = encounterRepository;
        this.patientValidationService = patientValidationService;
    }

    @Transactional
    public EncounterResponse createEncounter(CreateEncounterRequest request) {
        logger.info("Creating new encounter for patient {}", request.getPatientId());

        try {
            // Business Rule Validation hinzufügen
            validateEncounterCreation(request);

            Encounter encounter = new Encounter(
                    request.getPatientId(),
                    request.getPractitionerId(),
                    request.getType(),
                    request.getEncounterDate(),
                    request.getBillingContext());

            encounter = encounterRepository.save(encounter);

            logger.info("Created encounter {} for patient {}",
                    encounter.getId(), request.getPatientId());

            return mapToResponse(encounter);

        } catch (BusinessRuleViolationException ex) {
            throw ex; // Re-throw business rule violations
        } catch (DataIntegrityViolationException ex) {
            logger.error("Data integrity violation while creating encounter: {}", ex.getMessage());
            throw new RuntimeException("Encounter creation failed due to data conflict", ex);
        } catch (Exception ex) {
            logger.error("Unexpected error creating encounter for patient {}: {}",
                    request.getPatientId(), ex.getMessage());
            throw new RuntimeException("Failed to create encounter", ex);
        }
    }

    @Transactional
    public EncounterResponse createEncounterWithPatientValidation(CreateEncounterRequest request) {
        logger.info("Creating encounter with patient validation for patient {}", request.getPatientId());

        try {
            // Enhanced Patient Validation
            validatePatientForEncounter(request.getPatientId());

            // Business Rule Validation
            validateEncounterCreation(request);

            // Check for conflicting appointments
            checkForConflictingEncounters(request);

            return createEncounter(request);

        } catch (PatientNotFoundException ex) {
            logger.warn("Patient validation failed: {}", ex.getMessage());
            throw ex; // Re-throw as-is
        } catch (PatientServiceUnavailableException ex) {
            logger.error("Patient service unavailable during validation: {}", ex.getMessage());
            throw ex; // Re-throw as-is
        } catch (EncounterConflictException ex) {
            logger.warn("Encounter conflict detected: {}", ex.getMessage());
            throw ex; // Re-throw as-is
        } catch (Exception ex) {
            logger.error("Unexpected error during encounter creation with validation: {}", ex.getMessage());
            throw new RuntimeException("Failed to create encounter with patient validation", ex);
        }
    }

    @Transactional(readOnly = true)
    public EncounterResponse getEncounter(UUID encounterId) {
        try {
            Encounter encounter = findEncounterOrThrow(encounterId);
            return mapToResponse(encounter);
        } catch (Exception ex) {
            logger.error("Error retrieving encounter {}: {}", encounterId, ex.getMessage());
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public Page<EncounterSummary> getPatientEncounters(UUID patientId, Pageable pageable) {
        try {
            logger.info("Retrieving encounters for patient {} with pagination", patientId);
            return encounterRepository.findByPatientIdOrderByEncounterDateDesc(patientId, pageable)
                    .map(this::mapToSummary);
        } catch (Exception ex) {
            logger.error("Error retrieving encounters for patient {}: {}", patientId, ex.getMessage());
            throw new RuntimeException("Failed to retrieve patient encounters", ex);
        }
    }

    @Transactional(readOnly = true)
    public Page<EncounterSummary> getPatientEncountersInDateRange(
            UUID patientId, LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable) {
        try {
            logger.info("Retrieving encounters for patient {} in date range {} to {}",
                    patientId, fromDate, toDate);

            // Validate date range
            if (fromDate.isAfter(toDate)) {
                throw new BusinessRuleViolationException("INVALID_DATE_RANGE",
                        "From date cannot be after to date");
            }

            return encounterRepository.findByPatientIdAndDateRange(
                    patientId, fromDate, toDate, pageable)
                    .map(this::mapToSummary);
        } catch (BusinessRuleViolationException ex) {
            throw ex; // Re-throw business rule violations
        } catch (Exception ex) {
            logger.error("Error retrieving encounters for patient {} in date range: {}",
                    patientId, ex.getMessage());
            throw new RuntimeException("Failed to retrieve encounters in date range", ex);
        }
    }

    @Transactional
    public EncounterResponse startEncounter(UUID encounterId) {
        try {
            Encounter encounter = encounterRepository.findById(encounterId)
                    .orElseThrow(() -> new EncounterNotFoundException(encounterId));

            // Validate current status
            if (encounter.getStatus() == EncounterStatus.IN_PROGRESS) {
                throw new EncounterAlreadyStartedException(encounterId);
            }

            if (encounter.getStatus() == EncounterStatus.COMPLETED) {
                throw new EncounterAlreadyCompletedException(encounterId);
            }

            if (encounter.getStatus() != EncounterStatus.PLANNED) {
                throw new InvalidEncounterStateException(
                        String.format("Cannot start encounter in status %s. Expected: PLANNED",
                                encounter.getStatus()));
            }

            encounter.startEncounter();
            encounter = encounterRepository.save(encounter);

            logger.info("Successfully started encounter {}", encounterId);
            return mapToResponse(encounter);

        } catch (EncounterNotFoundException | EncounterAlreadyStartedException | EncounterAlreadyCompletedException
                | InvalidEncounterStateException ex) {
            throw ex; // Re-throw specific exceptions
        } catch (Exception ex) {
            logger.error("Unexpected error starting encounter {}: {}", encounterId, ex.getMessage());
            throw new RuntimeException("Failed to start encounter", ex);
        }
    }

    @Transactional
    public EncounterResponse completeEncounter(UUID encounterId) {
        try {
            Encounter encounter = encounterRepository.findById(encounterId)
                    .orElseThrow(() -> new EncounterNotFoundException(encounterId));

            // Validate current status
            if (encounter.getStatus() == EncounterStatus.COMPLETED) {
                throw new EncounterAlreadyCompletedException(encounterId);
            }

            if (encounter.getStatus() != EncounterStatus.IN_PROGRESS) {
                throw new InvalidEncounterStateException(
                        String.format("Cannot complete encounter in status %s. Expected: IN_PROGRESS",
                                encounter.getStatus()));
            }

            encounter.completeEncounter();
            encounter = encounterRepository.save(encounter);

            logger.info("Successfully completed encounter {}", encounterId);
            return mapToResponse(encounter);

        } catch (EncounterNotFoundException | EncounterAlreadyCompletedException | InvalidEncounterStateException ex) {
            throw ex; // Re-throw specific exceptions
        } catch (Exception ex) {
            logger.error("Unexpected error completing encounter {}: {}", encounterId, ex.getMessage());
            throw new RuntimeException("Failed to complete encounter", ex);
        }
    }

    @Transactional
    public EncounterResponse addDocumentation(UUID encounterId, EncounterDocumentation documentation) {
        try {
            logger.info("Adding {} documentation to encounter {}",
                    documentation.getSoapSection(), encounterId);

            Encounter encounter = findEncounterOrThrow(encounterId);

            // Validate encounter status for documentation
            if (encounter.getStatus() == EncounterStatus.COMPLETED) {
                throw new InvalidEncounterStateException(
                        "Cannot add documentation to completed encounter");
            }

            // Validate documentation content
            validateDocumentationContent(documentation);

            encounter.addDocumentation(documentation);
            encounter = encounterRepository.save(encounter);

            logger.info("Successfully added {} documentation to encounter {}",
                    documentation.getSoapSection(), encounterId);
            return mapToResponse(encounter);

        } catch (EncounterNotFoundException | InvalidEncounterStateException | InvalidDocumentationException ex) {
            throw ex; // Re-throw specific exceptions
        } catch (OptimisticLockingFailureException ex) {
            logger.warn("Concurrent modification detected for encounter {}", encounterId);
            throw new EncounterConcurrentModificationException(encounterId);
        } catch (Exception ex) {
            logger.error("Unexpected error adding documentation to encounter {}: {}",
                    encounterId, ex.getMessage());
            throw new RuntimeException("Failed to add documentation", ex);
        }
    }

    // =========================================================================
    // PRIVATE HELPER METHODS WITH ENHANCED VALIDATION
    // =========================================================================

    private Encounter findEncounterOrThrow(UUID encounterId) {
        return encounterRepository.findById(encounterId)
                .orElseThrow(() -> new EncounterNotFoundException(encounterId));
    }

    private void validatePatientForEncounter(UUID patientId) {
        try {
            if (!patientValidationService.validatePatientExists(patientId)) {
                throw new PatientNotFoundException("Patient not found: " + patientId);
            }

            // Optional: Get patient details for additional validation
            var patientDetails = patientValidationService.getPatientDetails(patientId);
            if (patientDetails != null) {
                logger.info("Creating encounter for patient: {} {}",
                        patientDetails.getFirstName(), patientDetails.getLastName());
            }
        } catch (PatientNotFoundException ex) {
            throw ex; // Re-throw as-is
        } catch (Exception ex) {
            logger.error("Error validating patient {}: {}", patientId, ex.getMessage());
            throw new PatientServiceUnavailableException(
                    "Patient service temporarily unavailable: " + ex.getMessage(), ex);
        }
    }

    private void validateEncounterCreation(CreateEncounterRequest request) {
        if (request.getPatientId() == null) {
            throw new BusinessRuleViolationException("MISSING_PATIENT_ID", "Patient ID is required");
        }

        if (request.getPractitionerId() == null) {
            throw new BusinessRuleViolationException("MISSING_PRACTITIONER_ID", "Practitioner ID is required");
        }

        if (request.getEncounterDate() == null) {
            throw new BusinessRuleViolationException("MISSING_ENCOUNTER_DATE", "Encounter date is required");
        }

        // Business rule: Encounter cannot be scheduled in the past (except for
        // emergency cases)
        if (request.getEncounterDate().isBefore(LocalDateTime.now().minusHours(1)) &&
                !isEmergencyType(request.getType())) {
            throw new BusinessRuleViolationException("PAST_ENCOUNTER_DATE",
                    "Encounter cannot be scheduled in the past");
        }

        // Business rule: Encounter cannot be scheduled more than 1 year in advance
        if (request.getEncounterDate().isAfter(LocalDateTime.now().plusYears(1))) {
            throw new BusinessRuleViolationException("FUTURE_ENCOUNTER_DATE",
                    "Encounter cannot be scheduled more than 1 year in advance");
        }
    }

    private void checkForConflictingEncounters(CreateEncounterRequest request) {
        // Check for overlapping encounters for the same patient
        LocalDateTime startTime = request.getEncounterDate().minusMinutes(30);
        LocalDateTime endTime = request.getEncounterDate().plusMinutes(30);

        List<Encounter> conflictingEncounters = encounterRepository
                .findByPatientIdAndDateRange(request.getPatientId(), startTime, endTime,
                        Pageable.unpaged())
                .getContent();

        if (!conflictingEncounters.isEmpty()) {
            throw new EncounterConflictException(request.getPatientId(),
                    request.getEncounterDate().toString());
        }
    }

    private void validateDocumentationForCompletion(Encounter encounter) {
        // Business rule: Certain encounter types require specific documentation
        if (encounter.getDocumentation().isEmpty()) {
            logger.warn("Completing encounter {} without any documentation", encounter.getId());
            // This could be made stricter based on business requirements
        }

        // Additional validation rules can be added here
        // e.g., check for required SOAP sections based on encounter type
    }

    private void validateDocumentationContent(EncounterDocumentation documentation) {
        if (documentation.getContent() == null || documentation.getContent().trim().isEmpty()) {
            throw new InvalidDocumentationException(documentation.getSoapSection().toString(),
                    "Content cannot be empty");
        }

        // Additional content validation rules
        if (documentation.getContent().length() > 10000) {
            throw new InvalidDocumentationException(documentation.getSoapSection().toString(),
                    "Content exceeds maximum length of 10000 characters");
        }
    }

    private boolean isEmergencyType(de.his.encounter.domain.model.EncounterType type) {
        return type == de.his.encounter.domain.model.EncounterType.EMERGENCY;
    }

    // =========================================================================
    // MAPPING METHODS (unchanged but with enhanced error handling)
    // =========================================================================

    private EncounterResponse mapToResponse(Encounter encounter) {
        try {
            List<de.his.encounter.application.dto.DocumentationResponse> documentation = encounter.getDocumentation()
                    .stream()
                    .map(this::mapDocumentationToResponse)
                    .collect(Collectors.toList());

            return new EncounterResponse(
                    encounter.getId(),
                    encounter.getPatientId(),
                    encounter.getPractitionerId(),
                    encounter.getType(),
                    encounter.getEncounterDate(),
                    encounter.getStatus(),
                    encounter.getBillingContext(),
                    documentation,
                    encounter.getCreatedAt(),
                    encounter.getUpdatedAt());
        } catch (Exception ex) {
            logger.error("Error mapping encounter to response: {}", ex.getMessage());
            throw new RuntimeException("Failed to map encounter response", ex);
        }
    }

    private EncounterSummary mapToSummary(Encounter encounter) {
        try {
            // Dokumentations-Anzahl berechnen
            Integer documentationCount = encounter.getDocumentation() != null ? encounter.getDocumentation().size() : 0;

            return new EncounterSummary(
                    encounter.getId(),
                    encounter.getType(),
                    encounter.getEncounterDate(),
                    encounter.getStatus(),
                    documentationCount);
        } catch (Exception ex) {
            logger.error("Error mapping encounter to summary: {}", ex.getMessage());
            throw new RuntimeException("Failed to map encounter summary", ex);
        }
    }

    private de.his.encounter.application.dto.DocumentationResponse mapDocumentationToResponse(
            EncounterDocumentation documentation) {
        try {
            return new de.his.encounter.application.dto.DocumentationResponse(
                    documentation.getId(),
                    documentation.getSoapSection(),
                    documentation.getContentType(),
                    documentation.getContent(),
                    documentation.getStructuredContent(),
                    documentation.getAuthorId(),
                    documentation.getCreatedAt());
        } catch (Exception ex) {
            logger.error("Error mapping documentation to response: {}", ex.getMessage());
            throw new RuntimeException("Failed to map documentation response", ex);
        }
    }
}