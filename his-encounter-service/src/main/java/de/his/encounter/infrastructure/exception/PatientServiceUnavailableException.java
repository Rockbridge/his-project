package de.his.encounter.infrastructure.exception;

public class PatientServiceUnavailableException extends RuntimeException {

    public PatientServiceUnavailableException(String message) {
        super(message);
    }

    public PatientServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
