package de.his.patient.infrastructure.exception;

public class PatientServiceUnavailableException extends RuntimeException {
    
    private final String serviceComponent;
    private final String estimatedRecoveryTime;

    public PatientServiceUnavailableException(String serviceComponent, String estimatedRecoveryTime, Throwable cause) {
        super(String.format("Patient service component '%s' is temporarily unavailable. " +
              "Estimated recovery: %s", serviceComponent, estimatedRecoveryTime), cause);
        this.serviceComponent = serviceComponent;
        this.estimatedRecoveryTime = estimatedRecoveryTime;
    }

    public String getServiceComponent() { return serviceComponent; }
    public String getEstimatedRecoveryTime() { return estimatedRecoveryTime; }
}
