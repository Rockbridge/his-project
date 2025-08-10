package de.his.patient.infrastructure.exception;

public class UnauthorizedPatientAccessException extends RuntimeException {
    
    private final String requestedPatientId;
    private final String accessingUser;
    private final String requiredPermission;

    public UnauthorizedPatientAccessException(String requestedPatientId, String accessingUser, String requiredPermission) {
        super(String.format("User '%s' is not authorized to access patient '%s'. Required permission: %s", 
              accessingUser, requestedPatientId, requiredPermission));
        this.requestedPatientId = requestedPatientId;
        this.accessingUser = accessingUser;
        this.requiredPermission = requiredPermission;
    }

    public String getRequestedPatientId() { return requestedPatientId; }
    public String getAccessingUser() { return accessingUser; }
    public String getRequiredPermission() { return requiredPermission; }
}
