package de.his.encounter.infrastructure.exception;

import java.util.UUID;

public class DocumentationNotFoundException extends RuntimeException {

    public DocumentationNotFoundException(UUID documentationId) {
        super(String.format("Documentation with ID %s not found", documentationId));
    }

    public DocumentationNotFoundException(UUID encounterId, String soapSection) {
        super(String.format("Documentation for SOAP section '%s' not found in encounter %s",
                soapSection, encounterId));
    }

    public DocumentationNotFoundException(String message) {
        super(message);
    }
}