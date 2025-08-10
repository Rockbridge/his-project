package de.his.encounter.infrastructure.exception;

import java.util.Map;

public class EncounterValidationException extends RuntimeException {

    private final Map<String, String> validationErrors;

    public EncounterValidationException(String message, Map<String, String> validationErrors) {
        super(message);
        this.validationErrors = validationErrors;
    }

    public Map<String, String> getValidationErrors() {
        return validationErrors;
    }
}
