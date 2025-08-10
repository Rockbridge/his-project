package de.his.encounter.infrastructure.exception;

import java.util.UUID;

public class EncounterNotFoundException extends RuntimeException {

    public EncounterNotFoundException(UUID encounterId) {
        super(String.format("Encounter with ID %s not found", encounterId));
    }
}
