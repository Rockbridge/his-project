package de.his.patient.infrastructure.exception;

public class PatientDataIntegrityException extends RuntimeException {
    
    private final String constraint;
    private final String conflictingData;

    public PatientDataIntegrityException(String constraint, String conflictingData, Throwable cause) {
        super(String.format("Data integrity violation in constraint '%s'. Conflicting data: %s", 
              constraint, conflictingData), cause);
        this.constraint = constraint;
        this.conflictingData = conflictingData;
    }

    public String getConstraint() { return constraint; }
    public String getConflictingData() { return conflictingData; }
}
