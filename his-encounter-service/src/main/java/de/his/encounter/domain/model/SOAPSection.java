package de.his.encounter.domain.model;

public enum SOAPSection {
    SUBJECTIVE("Subjektiv"),
    OBJECTIVE("Objektiv"),
    ASSESSMENT("Beurteilung"),
    PLAN("Plan");

    private final String displayName;

    SOAPSection(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
