package de.his.encounter.infrastructure.exception;

import java.util.UUID;

public class EncounterAlreadyStartedException extends RuntimeException {

    public EncounterAlreadyStartedException(UUID encounterId) {
        super(String.format("Encounter with ID %s has already been started", encounterId));
    }

    public EncounterAlreadyStartedException(String message) {
        super(message);
    }
}