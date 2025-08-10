package de.his.encounter;

import de.his.encounter.application.dto.CreateEncounterRequest;
import de.his.encounter.application.dto.EncounterResponse;
import de.his.encounter.application.service.EncounterService;
import de.his.encounter.domain.model.BillingContext;
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
class ServiceDirectTest {

    @Autowired
    private EncounterService encounterService;

    @Test
    void shouldCreateAndRetrieveEncounterDirectly() {
        try {
            // Given
            CreateEncounterRequest request = new CreateEncounterRequest(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    EncounterType.INITIAL,
                    LocalDateTime.now().plusDays(1),
                    BillingContext.GKV);

            System.out.println("🔍 Creating encounter with request: " + request.getType());

            // When - Create encounter
            EncounterResponse created = encounterService.createEncounter(request);
            System.out.println("✅ Created encounter: " + created.getId());

            // When - Retrieve encounter
            System.out.println("🔍 Retrieving encounter: " + created.getId());
            EncounterResponse retrieved = encounterService.getEncounter(created.getId());
            System.out.println("✅ Retrieved encounter: " + retrieved.getId());

            // Then
            assertThat(retrieved.getId()).isEqualTo(created.getId());
            assertThat(retrieved.getType()).isEqualTo(EncounterType.INITIAL);

            System.out.println("✅ Service direct test passed!");

        } catch (Exception e) {
            System.err.println("❌ Error in service direct test: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
