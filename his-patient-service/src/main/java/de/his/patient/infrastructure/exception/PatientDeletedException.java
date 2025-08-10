package de.his.patient.infrastructure.exception;

import java.time.LocalDateTime;

public class PatientDeletedException extends RuntimeException {
    
    private final String patientId;
    private final LocalDateTime deletedAt;
    private final String deletedBy;

    public PatientDeletedException(String patientId, LocalDateTime deletedAt, String deletedBy) {
        super(String.format("Patient '%s' was soft-deleted on %s by user '%s'. " +
              "Use restore endpoint to reactivate or contact administrator", 
              patientId, deletedAt, deletedBy));
        this.patientId = patientId;
        this.deletedAt = deletedAt;
        this.deletedBy = deletedBy;
    }

    public String getPatientId() { return patientId; }
    public LocalDateTime getDeletedAt() { return deletedAt; }
    public String getDeletedBy() { return deletedBy; }
}
