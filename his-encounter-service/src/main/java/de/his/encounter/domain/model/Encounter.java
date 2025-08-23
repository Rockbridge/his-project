package de.his.encounter.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "encounters",
        schema = "his_encounter",
        indexes = {
                @Index(name = "idx_patient_date", columnList = "patient_id, encounter_date"),
                @Index(name = "idx_encounter_date", columnList = "encounter_date"),
                @Index(name = "idx_status", columnList = "status")
        }
)
public class Encounter {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "encounter_id")
    private UUID id;

    @NotNull
    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @NotNull
    @Column(name = "practitioner_id", nullable = false)
    private UUID practitionerId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "encounter_type", nullable = false)
    private EncounterType type;

    @NotNull
    @Column(name = "encounter_date", nullable = false)
    private LocalDateTime encounterDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EncounterStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_context")
    private BillingContext billingContext;

    @OneToMany(mappedBy = "encounter", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EncounterDocumentation> documentation = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    private Long version;

    // Constructors
    public Encounter() {
    }

    public Encounter(UUID patientId, UUID practitionerId, EncounterType type,
            LocalDateTime encounterDate, BillingContext billingContext) {
        this.patientId = patientId;
        this.practitionerId = practitionerId;
        this.type = type;
        this.encounterDate = encounterDate;
        this.billingContext = billingContext;
        this.status = EncounterStatus.PLANNED;
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

    // KORREKTUR: Verwendet EncounterDocumentation statt DocumentationResponse
    public List<EncounterDocumentation> getDocumentation() {
        return documentation;
    }

    public void setDocumentation(List<EncounterDocumentation> documentation) {
        this.documentation = documentation;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Long getVersion() {
        return version;
    }

    // Business Methods
    public void addDocumentation(EncounterDocumentation doc) {
        doc.setEncounter(this);
        this.documentation.add(doc);
    }

    public void startEncounter() {
        if (this.status != EncounterStatus.PLANNED) {
            throw new IllegalStateException("Encounter can only be started from PLANNED status");
        }
        this.status = EncounterStatus.IN_PROGRESS;
    }

    public void completeEncounter() {
        if (this.status != EncounterStatus.IN_PROGRESS) {
            throw new IllegalStateException("Encounter can only be completed from IN_PROGRESS status");
        }
        this.status = EncounterStatus.COMPLETED;
    }
}