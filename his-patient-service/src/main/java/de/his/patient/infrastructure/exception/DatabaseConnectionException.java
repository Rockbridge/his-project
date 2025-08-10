package de.his.patient.infrastructure.exception;

public class DatabaseConnectionException extends RuntimeException {
    
    private final String operation;
    private final String details;

    public DatabaseConnectionException(String operation, String details, Throwable cause) {
        super(String.format("Database operation '%s' failed. Details: %s", operation, details), cause);
        this.operation = operation;
        this.details = details;
    }

    public DatabaseConnectionException(String operation, Throwable cause) {
        super(String.format("Database operation '%s' failed due to connection issues", operation), cause);
        this.operation = operation;
        this.details = "Connection timeout or network error";
    }

    public String getOperation() { return operation; }
    public String getDetails() { return details; }
}
