package de.his.encounter.infrastructure.exception;

public class InvalidEncounterStateException extends RuntimeException {

    public InvalidEncounterStateException(String message) {
        super(message);
    }
}
