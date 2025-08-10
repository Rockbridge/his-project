package de.his.encounter.application.dto;

import de.his.encounter.domain.model.SOAPSection;
import de.his.encounter.domain.model.ContentType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "SOAP documentation entry")
public class DocumentationResponse {

    @Schema(description = "Documentation ID")
    private UUID id;

    @Schema(description = "SOAP section")
    private SOAPSection soapSection;

    @Schema(description = "Content type")
    private ContentType contentType;

    @Schema(description = "Documentation content")
    private String content;

    @Schema(description = "Structured content (JSON)")
    private String structuredContent;

    @Schema(description = "Author ID")
    private UUID authorId;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    // Constructor
    public DocumentationResponse(UUID id, SOAPSection soapSection, ContentType contentType,
            String content, String structuredContent, UUID authorId, LocalDateTime createdAt) {
        this.id = id;
        this.soapSection = soapSection;
        this.contentType = contentType;
        this.content = content;
        this.structuredContent = structuredContent;
        this.authorId = authorId;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}