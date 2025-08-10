package de.his.patient.infrastructure.exception;

public class PatientNotFoundException extends RuntimeException {
    public PatientNotFoundException(String identifier) {
        super("Patient not found: " + identifier);
    }
}
