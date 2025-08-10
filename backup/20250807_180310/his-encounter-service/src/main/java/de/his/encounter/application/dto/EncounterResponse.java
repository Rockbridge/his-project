package de.his.encounter.application.dto;

import de.his.encounter.domain.model.BillingContext;
import de.his.encounter.domain.model.EncounterStatus;
import de.his.encounter.domain.model.EncounterType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Complete encounter information")
public class EncounterResponse {

    @Schema(description = "Unique encounter identifier")
    private UUID id;

    @Schema(description = "Patient identifier")
    private UUID patientId;

    @Schema(description = "Practitioner identifier")
    private UUID practitionerId;

    @Schema(description = "Type of encounter")
    private EncounterType type;

    @Schema(description = "Date and time of encounter")
    private LocalDateTime encounterDate;

    @Schema(description = "Current status of encounter")
    private EncounterStatus status;

    @Schema(description = "Billing context")
    private BillingContext billingContext;

    @Schema(description = "SOAP documentation entries")
    private List<DocumentationResponse> documentation;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    // Constructor
    public EncounterResponse(UUID id, UUID patientId, UUID practitionerId, EncounterType type,
            LocalDateTime encounterDate, EncounterStatus status, BillingContext billingContext,
            List<DocumentationResponse> documentation, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.patientId = patientId;
        this.practitionerId = practitionerId;
        this.type = type;
        this.encounterDate = encounterDate;
        this.status = status;
        this.billingContext = billingContext;
        this.documentation = documentation;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

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

    public EncounterStatus getStatus() {
        return status;
    }

    public void setStatus(EncounterStatus status) {
        this.status = status;
    }

    public BillingContext getBillingContext() {
        return billingContext;
    }

    public void setBillingContext(BillingContext billingContext) {
        this.billingContext = billingContext;
    }

    public List<DocumentationResponse> getDocumentation() {
        return documentation;
    }

    public void setDocumentation(List<DocumentationResponse> documentation) {
        this.documentation = documentation;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
