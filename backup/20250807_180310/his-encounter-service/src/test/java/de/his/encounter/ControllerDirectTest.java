package de.his.encounter;

import de.his.encounter.application.dto.CreateEncounterRequest;
import de.his.encounter.application.dto.EncounterResponse;
import de.his.encounter.domain.model.BillingContext;
import de.his.encounter.domain.model.EncounterType;
import de.his.encounter.presentation.controller.EncounterController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ControllerDirectTest {

    @Autowired
    private EncounterController encounterController;

    @Test
    void shouldCreateAndRetrieveEncounterViaController() {
        try {
            // Given
            CreateEncounterRequest request = new CreateEncounterRequest(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    EncounterType.INITIAL,
                    LocalDateTime.now().plusDays(1),
                    BillingContext.GKV);

            System.out.println("🔍 Creating encounter via controller");

            // When - Create encounter
            ResponseEntity<EncounterResponse> createResponse = encounterController.createEncounter(request);
            System.out.println("✅ Controller create response: " + createResponse.getStatusCode());

            assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(createResponse.getBody()).isNotNull();

            UUID encounterId = createResponse.getBody().getId();
            System.out.println("✅ Created encounter via controller: " + encounterId);

            // When - Retrieve encounter
            System.out.println("🔍 Retrieving encounter via controller: " + encounterId);
            ResponseEntity<EncounterResponse> getResponse = encounterController.getEncounter(encounterId);
            System.out.println("✅ Controller get response: " + getResponse.getStatusCode());

            // Then
            assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(getResponse.getBody()).isNotNull();
            assertThat(getResponse.getBody().getId()).isEqualTo(encounterId);

            System.out.println("✅ Controller direct test passed!");

        } catch (Exception e) {
            System.err.println("❌ Error in controller direct test: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
