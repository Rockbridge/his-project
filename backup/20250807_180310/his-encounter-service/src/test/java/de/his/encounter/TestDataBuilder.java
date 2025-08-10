package de.his.encounter;

import de.his.encounter.application.dto.CreateEncounterRequest;
import de.his.encounter.domain.model.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Builder-Klasse für Test-Daten
 */
public class TestDataBuilder {

    public static CreateEncounterRequest.Builder createEncounterRequest() {
        return new CreateEncounterRequest.Builder();
    }

    public static Encounter.Builder encounter() {
        return new Encounter.Builder();
    }

    public static EncounterDocumentation.Builder encounterDocumentation() {
        return new EncounterDocumentation.Builder();
    }

    public static class CreateEncounterRequest {
        public static class Builder {
            private UUID patientId = UUID.randomUUID();
            private UUID practitionerId = UUID.randomUUID();
            private EncounterType type = EncounterType.INITIAL;
            private LocalDateTime encounterDate = LocalDateTime.now().plusDays(1);
            private BillingContext billingContext = BillingContext.GKV;

            public Builder patientId(UUID patientId) {
                this.patientId = patientId;
                return this;
            }

            public Builder practitionerId(UUID practitionerId) {
                this.practitionerId = practitionerId;
                return this;
            }

            public Builder type(EncounterType type) {
                this.type = type;
                return this;
            }

            public Builder encounterDate(LocalDateTime encounterDate) {
                this.encounterDate = encounterDate;
                return this;
            }

            public Builder billingContext(BillingContext billingContext) {
                this.billingContext = billingContext;
                return this;
            }

            public de.his.encounter.application.dto.CreateEncounterRequest build() {
                return new de.his.encounter.application.dto.CreateEncounterRequest(
                        patientId, practitionerId, type, encounterDate, billingContext);
            }
        }
    }

    public static class Encounter {
        public static class Builder {
            private UUID patientId = UUID.randomUUID();
            private UUID practitionerId = UUID.randomUUID();
            private EncounterType type = EncounterType.INITIAL;
            private LocalDateTime encounterDate = LocalDateTime.now().plusDays(1);
            private BillingContext billingContext = BillingContext.GKV;

            public Builder patientId(UUID patientId) {
                this.patientId = patientId;
                return this;
            }

            public Builder practitionerId(UUID practitionerId) {
                this.practitionerId = practitionerId;
                return this;
            }

            public Builder type(EncounterType type) {
                this.type = type;
                return this;
            }

            public Builder encounterDate(LocalDateTime encounterDate) {
                this.encounterDate = encounterDate;
                return this;
            }

            public Builder billingContext(BillingContext billingContext) {
                this.billingContext = billingContext;
                return this;
            }

            public de.his.encounter.domain.model.Encounter build() {
                return new de.his.encounter.domain.model.Encounter(
                        patientId, practitionerId, type, encounterDate, billingContext);
            }
        }
    }

    public static class EncounterDocumentation {
        public static class Builder {
            private SOAPSection soapSection = SOAPSection.SUBJECTIVE;
            private ContentType contentType = ContentType.TEXT;
            private String content = "Test content";
            private UUID authorId = UUID.randomUUID();

            public Builder soapSection(SOAPSection soapSection) {
                this.soapSection = soapSection;
                return this;
            }

            public Builder contentType(ContentType contentType) {
                this.contentType = contentType;
                return this;
            }

            public Builder content(String content) {
                this.content = content;
                return this;
            }

            public Builder authorId(UUID authorId) {
                this.authorId = authorId;
                return this;
            }

            public de.his.encounter.domain.model.EncounterDocumentation build() {
                return new de.his.encounter.domain.model.EncounterDocumentation(
                        soapSection, contentType, content, authorId);
            }
        }
    }
}
