package de.his.encounter.domain.model;

public enum BillingContext {
    GKV("Gesetzliche Krankenversicherung"),
    PKV("Private Krankenversicherung"),
    SELF_PAY("Selbstzahler"),
    BG("Berufsgenossenschaft");

    private final String displayName;

    BillingContext(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
