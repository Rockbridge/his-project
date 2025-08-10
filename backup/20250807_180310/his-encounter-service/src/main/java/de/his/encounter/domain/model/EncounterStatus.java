package de.his.encounter.domain.model;

public enum EncounterStatus {
    PLANNED("Geplant"),
    IN_PROGRESS("Laufend"),
    COMPLETED("Abgeschlossen"),
    CANCELLED("Abgebrochen");

    private final String displayName;

    EncounterStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
