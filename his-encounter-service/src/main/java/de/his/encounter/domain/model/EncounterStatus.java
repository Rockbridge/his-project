package de.his.encounter.domain.model;

public enum EncounterStatus {
    PLANNED("Planned"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled"),
    NO_SHOW("No Show"),
    POSTPONED("Postponed");

    private final String description;

    EncounterStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}