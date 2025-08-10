package de.his.encounter;

import de.his.encounter.application.dto.CreateEncounterRequest;
import de.his.encounter.application.dto.EncounterResponse;
import de.his.encounter.application.service.EncounterService;
import de.his.encounter.domain.model.BillingContext;
import de.his.encounter.domain.model.EncounterStatus;
import de.his.encounter.domain.model.EncounterType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ServiceIntegrationTest {

    @Autowired
    private EncounterService encounterService;

    @Test
    void shouldCreateAndRetrieveEncounter() {
        // Given
        CreateEncounterRequest request = new CreateEncounterRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                EncounterType.INITIAL,
                LocalDateTime.now().plusDays(1),
                BillingContext.GKV);

        // When - Create encounter
        EncounterResponse created = encounterService.createEncounter(request);

        // Then - Verify creation
        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull();
        assertThat(created.getType()).isEqualTo(EncounterType.INITIAL);
        assertThat(created.getStatus()).isEqualTo(EncounterStatus.PLANNED);

        // When - Retrieve encounter
        EncounterResponse retrieved = encounterService.getEncounter(created.getId());

        // Then - Verify retrieval
        assertThat(retrieved.getId()).isEqualTo(created.getId());
        assertThat(retrieved.getType()).isEqualTo(EncounterType.INITIAL);
    }

    @Test
    void shouldHandleEncounterLifecycle() {
        // Given
        CreateEncounterRequest request = new CreateEncounterRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                EncounterType.EMERGENCY,
                LocalDateTime.now(),
                BillingContext.GKV);

        EncounterResponse created = encounterService.createEncounter(request);

        // When - Start and complete encounter
        EncounterResponse started = encounterService.startEncounter(created.getId());
        EncounterResponse completed = encounterService.completeEncounter(created.getId());

        // Then
        assertThat(started.getStatus()).isEqualTo(EncounterStatus.IN_PROGRESS);
        assertThat(completed.getStatus()).isEqualTo(EncounterStatus.COMPLETED);
    }
}
