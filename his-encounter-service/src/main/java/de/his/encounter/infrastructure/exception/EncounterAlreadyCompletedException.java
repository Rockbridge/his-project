package de.his.encounter.infrastructure.exception;

import java.util.UUID;

public class EncounterAlreadyCompletedException extends RuntimeException {

    public EncounterAlreadyCompletedException(UUID encounterId) {
        super(String.format("Encounter with ID %s has already been completed", encounterId));
    }

    public EncounterAlreadyCompletedException(String message) {
        super(message);
    }
}