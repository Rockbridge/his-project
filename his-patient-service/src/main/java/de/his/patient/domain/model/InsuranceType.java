package de.his.patient.domain.model;

public enum InsuranceType {
    STATUTORY("GKV"),
    PRIVATE("PKV"),
    SELF_PAYER("Selbstzahler"),
    OTHER("Sonstige");

    private final String description;

    InsuranceType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
