package de.his.patient.infrastructure.exception;

public class PatientAlreadyExistsException extends RuntimeException {
    public PatientAlreadyExistsException(String kvnr) {
        super("Patient with KVNR already exists: " + kvnr);
    }
}
