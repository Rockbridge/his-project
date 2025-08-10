package de.his.encounter.infrastructure.exception;

import java.util.UUID;

public class UnauthorizedEncounterAccessException extends RuntimeException {

    public UnauthorizedEncounterAccessException(UUID encounterId, UUID userId) {
        super(String.format("User %s is not authorized to access encounter %s", userId, encounterId));
    }

    public UnauthorizedEncounterAccessException(String message) {
        super(message);
    }
}