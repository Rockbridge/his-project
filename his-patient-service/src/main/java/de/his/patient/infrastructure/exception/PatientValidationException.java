package de.his.patient.infrastructure.exception;

public class PatientValidationException extends RuntimeException {
    
    private final String field;
    private final String rejectedValue;
    private final String reason;

    public PatientValidationException(String field, String rejectedValue, String reason) {
        super(String.format("Invalid value for field '%s': '%s'. Reason: %s", field, rejectedValue, reason));
        this.field = field;
        this.rejectedValue = rejectedValue;
        this.reason = reason;
    }

    public PatientValidationException(String message) {
        super(message);
        this.field = null;
        this.rejectedValue = null;
        this.reason = null;
    }

    public String getField() { return field; }
    public String getRejectedValue() { return rejectedValue; }
    public String getReason() { return reason; }
}
