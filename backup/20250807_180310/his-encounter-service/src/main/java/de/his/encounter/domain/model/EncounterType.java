package de.his.encounter.domain.model;

public enum EncounterType {
    INITIAL("Erstbesuch"),
    FOLLOW_UP("Folgebesuch"),
    EMERGENCY("Notfall"),
    CONSULTATION("Konsultation"),
    PREVENTIVE("Vorsorge");

    private final String displayName;

    EncounterType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
