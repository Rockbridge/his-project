package de.his.encounter.infrastructure.exception;

public class InvalidDocumentationException extends RuntimeException {

    public InvalidDocumentationException(String reason) {
        super(String.format("Invalid documentation: %s", reason));
    }

    public InvalidDocumentationException(String soapSection, String reason) {
        super(String.format("Invalid documentation for SOAP section '%s': %s", soapSection, reason));
    }
}
