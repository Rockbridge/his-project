package de.his.encounter.application.service;

import de.his.encounter.application.dto.CreateEncounterRequest;
import de.his.encounter.application.dto.EncounterResponse;
import de.his.encounter.application.dto.EncounterSummary;
import de.his.encounter.application.dto.DocumentationResponse;
import de.his.encounter.domain.model.Encounter;
import de.his.encounter.domain.model.EncounterDocumentation;
import de.his.encounter.domain.repository.EncounterRepository;
import de.his.encounter.infrastructure.exception.EncounterNotFoundException;
import de.his.encounter.infrastructure.exception.PatientNotFoundException;
import de.his.encounter.infrastructure.service.PatientValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    }

    @Transactional
    public EncounterResponse createEncounterWithPatientValidation(CreateEncounterRequest request) {
        logger.info("Creating encounter with patient validation for patient {}", request.getPatientId());

        // Patient-Validierung über Feign Client durchführen
        try {
            if (!patientValidationService.validatePatientExists(request.getPatientId())) {
                logger.warn("Patient validation failed - patient not found: {}", request.getPatientId());
                throw new PatientNotFoundException("Patient not found: " + request.getPatientId());
            }

            logger.info("Patient validation successful for patient {}", request.getPatientId());

            // Optional: Patient-Details für Logging abrufen
            var patientDetails = patientValidationService.getPatientDetails(request.getPatientId());
            if (patientDetails != null) {
                logger.info("Creating encounter for patient: {} {}",
                        patientDetails.getFirstName(), patientDetails.getLastName());
            }

        } catch (PatientNotFoundException e) {
            logger.error("Patient validation failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error during patient validation for {}: {}", request.getPatientId(), e.getMessage());
            throw new RuntimeException("Patient service temporarily unavailable", e);
        }

        // Encounter normal erstellen nach erfolgreicher Validierung
        return createEncounter(request);
    }

    @Transactional(readOnly = true)
    public EncounterResponse getEncounter(UUID encounterId) {
        Encounter encounter = encounterRepository.findById(encounterId)
                .orElseThrow(() -> new EncounterNotFoundException(encounterId));

        return mapToResponse(encounter);
    }

    @Transactional(readOnly = true)
    public Page<EncounterSummary> getPatientEncounters(UUID patientId, Pageable pageable) {
        return encounterRepository.findByPatientIdOrderByEncounterDateDesc(patientId, pageable)
                .map(this::mapToSummary);
    }

    @Transactional(readOnly = true)
    public Page<EncounterSummary> getPatientEncountersInDateRange(
            UUID patientId, LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable) {

        return encounterRepository.findByPatientIdAndDateRange(
                patientId, fromDate, toDate, pageable)
                .map(this::mapToSummary);
    }

    @Transactional
    public EncounterResponse startEncounter(UUID encounterId) {
        Encounter encounter = getEncounterEntity(encounterId);
        encounter.startEncounter();

        encounter = encounterRepository.save(encounter);
        logger.info("Started encounter {}", encounterId);

        return mapToResponse(encounter);
    }

    @Transactional
    public EncounterResponse completeEncounter(UUID encounterId) {
        Encounter encounter = getEncounterEntity(encounterId);
        encounter.completeEncounter();

        encounter = encounterRepository.save(encounter);
        logger.info("Completed encounter {}", encounterId);

        return mapToResponse(encounter);
    }

    @Transactional
    public EncounterResponse addDocumentation(UUID encounterId,
            EncounterDocumentation documentation) {
        Encounter encounter = getEncounterEntity(encounterId);
        encounter.addDocumentation(documentation);

        encounter = encounterRepository.save(encounter);
        logger.info("Added {} documentation to encounter {}",
                documentation.getSoapSection(), encounterId);

        return mapToResponse(encounter);
    }

    private Encounter getEncounterEntity(UUID encounterId) {
        return encounterRepository.findById(encounterId)
                .orElseThrow(() -> new EncounterNotFoundException(encounterId));
    }

    private EncounterResponse mapToResponse(Encounter encounter) {
        return new EncounterResponse(
                encounter.getId(),
                encounter.getPatientId(),
                encounter.getPractitionerId(),
                encounter.getType(),
                encounter.getEncounterDate(),
                encounter.getStatus(),
                encounter.getBillingContext(),
                encounter.getDocumentation().stream()
                        .map(this::mapDocumentationToDto)
                        .collect(Collectors.toList()),
                encounter.getCreatedAt(),
                encounter.getUpdatedAt());
    }

    private EncounterSummary mapToSummary(Encounter encounter) {
        return new EncounterSummary(
                encounter.getId(),
                encounter.getType(),
                encounter.getEncounterDate(),
                encounter.getStatus(),
                encounter.getDocumentation().size());
    }

    private DocumentationResponse mapDocumentationToDto(EncounterDocumentation doc) {
        return new DocumentationResponse(
                doc.getId(),
                doc.getSoapSection(),
                doc.getContentType(),
                doc.getContent(),
                doc.getStructuredContent(),
                doc.getAuthorId(),
                doc.getCreatedAt());
    }
}