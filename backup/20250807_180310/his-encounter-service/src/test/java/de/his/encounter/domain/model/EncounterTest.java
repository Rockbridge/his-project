package de.his.encounter.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class EncounterTest {

    private UUID patientId;
    private UUID practitionerId;
    private LocalDateTime encounterDate;
    private Encounter encounter;

    @BeforeEach
    void setUp() {
        patientId = UUID.randomUUID();
        practitionerId = UUID.randomUUID();
        encounterDate = LocalDateTime.now().plusDays(1);

        encounter = new Encounter(
                patientId, practitionerId, EncounterType.INITIAL,
                encounterDate, BillingContext.GKV);
    }

    @Test
    void shouldCreateEncounterWithPlannedStatus() {
        // Then
        assertThat(encounter.getPatientId()).isEqualTo(patientId);
        assertThat(encounter.getPractitionerId()).isEqualTo(practitionerId);
        assertThat(encounter.getType()).isEqualTo(EncounterType.INITIAL);
        assertThat(encounter.getStatus()).isEqualTo(EncounterStatus.PLANNED);
        assertThat(encounter.getBillingContext()).isEqualTo(BillingContext.GKV);
        assertThat(encounter.getDocumentation()).isEmpty();
    }

    @Test
    void shouldStartEncounterFromPlannedStatus() {
        // When
        encounter.startEncounter();

        // Then
        assertThat(encounter.getStatus()).isEqualTo(EncounterStatus.IN_PROGRESS);
    }

    @Test
    void shouldThrowExceptionWhenStartingNonPlannedEncounter() {
        // Given
        encounter.startEncounter(); // Now IN_PROGRESS

        // When & Then
        assertThatThrownBy(() -> encounter.startEncounter())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be started from PLANNED status");
    }

    @Test
    void shouldCompleteEncounterFromInProgressStatus() {
        // Given
        encounter.startEncounter(); // First start it

        // When
        encounter.completeEncounter();

        // Then
        assertThat(encounter.getStatus()).isEqualTo(EncounterStatus.COMPLETED);
    }

    @Test
    void shouldThrowExceptionWhenCompletingNonInProgressEncounter() {
        // When & Then (still PLANNED)
        assertThatThrownBy(() -> encounter.completeEncounter())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be completed from IN_PROGRESS status");
    }

    @Test
    void shouldAddDocumentation() {
        // Given
        EncounterDocumentation documentation = new EncounterDocumentation(
                SOAPSection.SUBJECTIVE, ContentType.TEXT, "Patient complaints", UUID.randomUUID());

        // When
        encounter.addDocumentation(documentation);

        // Then
        assertThat(encounter.getDocumentation()).hasSize(1);
        assertThat(encounter.getDocumentation().get(0)).isEqualTo(documentation);
        assertThat(documentation.getEncounter()).isEqualTo(encounter);
    }

    @Test
    void shouldHandleMultipleDocumentationEntries() {
        // Given
        EncounterDocumentation subjective = new EncounterDocumentation(
                SOAPSection.SUBJECTIVE, ContentType.TEXT, "Patient complaints", UUID.randomUUID());
        EncounterDocumentation objective = new EncounterDocumentation(
                SOAPSection.OBJECTIVE, ContentType.TEXT, "Examination findings", UUID.randomUUID());

        // When
        encounter.addDocumentation(subjective);
        encounter.addDocumentation(objective);

        // Then
        assertThat(encounter.getDocumentation()).hasSize(2);
        assertThat(encounter.getDocumentation()).containsExactly(subjective, objective);
    }
}
