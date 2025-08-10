package de.his.encounter.domain.model;

public enum ContentType {
    TEXT("Text"),
    STRUCTURED_DATA("Strukturierte Daten");

    private final String displayName;

    ContentType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
