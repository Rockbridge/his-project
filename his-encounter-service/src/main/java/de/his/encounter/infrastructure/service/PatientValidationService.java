package de.his.encounter.infrastructure.service;

import de.his.encounter.infrastructure.client.PatientClient;
import de.his.encounter.infrastructure.client.dto.PatientDto;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PatientValidationService {

    private static final Logger logger = LoggerFactory.getLogger(PatientValidationService.class);

    private final PatientClient patientClient;

    public PatientValidationService(PatientClient patientClient) {
        this.patientClient = patientClient;
    }

    public boolean validatePatientExists(UUID patientId) {
        try {
            logger.info("Validating patient exists for ID: {}", patientId);
            PatientDto patient = patientClient.getPatient(patientId);
            logger.info("Patient validation successful for ID: {}", patientId);
            return patient != null;
        } catch (FeignException.NotFound e) {
            logger.warn("Patient not found for ID: {} - HTTP Status: {}", patientId, e.status());
            return false;
        } catch (FeignException e) {
            logger.error("Feign error validating patient with ID {}: HTTP Status: {}, Message: {}",
                    patientId, e.status(), e.getMessage());
            throw new RuntimeException("Patient service temporarily unavailable: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error validating patient with ID {}: {}", patientId, e.getMessage(), e);
            throw new RuntimeException("Patient validation failed: " + e.getMessage(), e);
        }
    }

    public PatientDto getPatientDetails(UUID patientId) {
        try {
            return patientClient.getPatient(patientId);
        } catch (FeignException.NotFound e) {
            logger.warn("Patient not found for ID: {}", patientId);
            return null;
        } catch (FeignException e) {
            logger.error("Error retrieving patient with ID {}: {}", patientId, e.getMessage());
            throw new RuntimeException("Patient service temporarily unavailable", e);
        }
    }
}