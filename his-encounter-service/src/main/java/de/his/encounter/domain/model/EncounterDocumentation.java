package de.his.encounter.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "encounter_documentation",
        schema = "his_encounter",
        indexes = {
                @Index(name = "idx_encounter_soap", columnList = "encounter_id, soap_section"),
                @Index(name = "idx_author_date", columnList = "author_id, created_at")
        }
)
public class EncounterDocumentation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "documentation_id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "encounter_id", nullable = false)
    private Encounter encounter;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "soap_section", nullable = false)
    private SOAPSection soapSection;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false)
    private ContentType contentType;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    // JSONB für PostgreSQL, TEXT für H2
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "structured_content", columnDefinition = "TEXT") // Geändert von "jsonb" zu "TEXT"
    private String structuredContent;

    @NotNull
    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Constructors
    public EncounterDocumentation() {
    }

    public EncounterDocumentation(SOAPSection soapSection, ContentType contentType,
            String content, UUID authorId) {
        this.soapSection = soapSection;
        this.contentType = contentType;
        this.content = content;
        this.authorId = authorId;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Encounter getEncounter() {
        return encounter;
    }

    public void setEncounter(Encounter encounter) {
        this.encounter = encounter;
    }

    public SOAPSection getSoapSection() {
        return soapSection;
    }

    public void setSoapSection(SOAPSection soapSection) {
        this.soapSection = soapSection;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getStructuredContent() {
        return structuredContent;
    }

    public void setStructuredContent(String structuredContent) {
        this.structuredContent = structuredContent;
    }

    public UUID getAuthorId() {
        return authorId;
    }

    public void setAuthorId(UUID authorId) {
        this.authorId = authorId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
