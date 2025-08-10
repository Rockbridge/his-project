package de.his.encounter.infrastructure.exception;

import java.util.UUID;

public class EncounterConcurrentModificationException extends RuntimeException {

    public EncounterConcurrentModificationException(UUID encounterId) {
        super(String.format("Encounter %s was modified by another user. Please refresh and try again.", encounterId));
    }

    public EncounterConcurrentModificationException(String message) {
        super(message);
    }
}