package de.his.patient.domain.model;

public enum Gender {
    MALE("male"),
    FEMALE("female"),
    OTHER("other"),
    UNKNOWN("unknown");

    private final String fhirCode;

    Gender(String fhirCode) {
        this.fhirCode = fhirCode;
    }

    public String getFhirCode() {
        return fhirCode;
    }
}
