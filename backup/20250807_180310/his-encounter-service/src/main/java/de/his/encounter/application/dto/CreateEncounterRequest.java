package de.his.encounter.application.dto;

import de.his.encounter.domain.model.BillingContext;
import de.his.encounter.domain.model.EncounterType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Request to create a new encounter")
public class CreateEncounterRequest {

    @NotNull(message = "Patient ID is required")
    @Schema(description = "ID of the patient", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID patientId;

    @NotNull(message = "Practitioner ID is required")
    @Schema(description = "ID of the practitioner", example = "987fcdeb-51d3-42a8-b456-123456789abc")
    private UUID practitionerId;

    @NotNull(message = "Encounter type is required")
    @Schema(description = "Type of encounter")
    private EncounterType type;

    @NotNull(message = "Encounter date is required")
    @Schema(description = "Date and time of the encounter", example = "2025-08-01T10:30:00")
    private LocalDateTime encounterDate;

    @Schema(description = "Billing context for the encounter")
    private BillingContext billingContext;

    // Constructors
    public CreateEncounterRequest() {
    }

    public CreateEncounterRequest(UUID patientId, UUID practitionerId, EncounterType type,
            LocalDateTime encounterDate, BillingContext billingContext) {
        this.patientId = patientId;
        this.practitionerId = practitionerId;
        this.type = type;
        this.encounterDate = encounterDate;
        this.billingContext = billingContext;
    }

    // Getters and Setters
    public UUID getPatientId() {
        return patientId;
    }

    public void setPatientId(UUID patientId) {
        this.patientId = patientId;
    }

    public UUID getPractitionerId() {
        return practitionerId;
    }

    public void setPractitionerId(UUID practitionerId) {
        this.practitionerId = practitionerId;
    }

    public EncounterType getType() {
        return type;
    }

    public void setType(EncounterType type) {
        this.type = type;
    }

    public LocalDateTime getEncounterDate() {
        return encounterDate;
    }

    public void setEncounterDate(LocalDateTime encounterDate) {
        this.encounterDate = encounterDate;
    }

    public BillingContext getBillingContext() {
        return billingContext;
    }

    public void setBillingContext(BillingContext billingContext) {
        this.billingContext = billingContext;
    }
}
