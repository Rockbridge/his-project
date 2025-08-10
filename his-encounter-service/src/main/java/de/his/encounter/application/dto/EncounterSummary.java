package de.his.encounter.application.dto;

import de.his.encounter.domain.model.EncounterStatus;
import de.his.encounter.domain.model.EncounterType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Summary view of encounter for timeline display")
public class EncounterSummary {

    @Schema(description = "Encounter identifier")
    private UUID id;

    @Schema(description = "Type of encounter")
    private EncounterType type;

    @Schema(description = "Date and time of encounter")
    private LocalDateTime encounterDate;

    @Schema(description = "Current status")
    private EncounterStatus status;

    @Schema(description = "Number of documentation entries")
    private Integer documentationCount;

    // Constructor
    public EncounterSummary(UUID id, EncounterType type, LocalDateTime encounterDate,
            EncounterStatus status, Integer documentationCount) {
        this.id = id;
        this.type = type;
        this.encounterDate = encounterDate;
        this.status = status;
        this.documentationCount = documentationCount;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public Integer getDocumentationCount() {
        return documentationCount;
    }

    public void setDocumentationCount(Integer documentationCount) {
        this.documentationCount = documentationCount;
    }
}
