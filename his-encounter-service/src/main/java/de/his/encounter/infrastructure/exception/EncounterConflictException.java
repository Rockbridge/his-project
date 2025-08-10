package de.his.encounter.infrastructure.exception;

import java.util.UUID;

public class EncounterConflictException extends RuntimeException {

    public EncounterConflictException(UUID patientId, String timeSlot) {
        super(String.format("Patient %s already has an encounter scheduled for %s", patientId, timeSlot));
    }

    public EncounterConflictException(String message) {
        super(message);
    }
}