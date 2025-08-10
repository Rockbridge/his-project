package de.his.patient.infrastructure.exception;

public class InvalidKvnrException extends RuntimeException {
    
    private final String kvnr;
    private final String validationError;

    public InvalidKvnrException(String kvnr, String validationError) {
        super(String.format("Invalid KVNR '%s': %s. KVNR must be exactly 10 digits", kvnr, validationError));
        this.kvnr = kvnr;
        this.validationError = validationError;
    }

    public String getKvnr() { return kvnr; }
    public String getValidationError() { return validationError; }
}
