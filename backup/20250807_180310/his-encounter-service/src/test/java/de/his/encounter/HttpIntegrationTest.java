package de.his.encounter;

import de.his.encounter.application.dto.CreateEncounterRequest;
import de.his.encounter.application.dto.EncounterResponse;
import de.his.encounter.domain.model.BillingContext;
import de.his.encounter.domain.model.EncounterStatus;
import de.his.encounter.domain.model.EncounterType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class HttpIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/v1/encounters";
    }

    @Test
    void shouldCreateEncounterViaHttp() {
        // Given
        CreateEncounterRequest request = new CreateEncounterRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                EncounterType.INITIAL,
                LocalDateTime.now().plusDays(1),
                BillingContext.GKV);

        // When
        ResponseEntity<EncounterResponse> response = restTemplate.postForEntity(
                getBaseUrl(), request, EncounterResponse.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getType()).isEqualTo(EncounterType.INITIAL);
        assertThat(response.getBody().getStatus()).isEqualTo(EncounterStatus.PLANNED);

        System.out.println("✅ Created encounter: " + response.getBody().getId());
    }

    @Test
    void shouldRetrieveEncounterViaHttp() {
        // Given - Create encounter first
        CreateEncounterRequest request = new CreateEncounterRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                EncounterType.FOLLOW_UP,
                LocalDateTime.now().plusHours(2),
                BillingContext.PKV);

        ResponseEntity<EncounterResponse> createResponse = restTemplate.postForEntity(
                getBaseUrl(), request, EncounterResponse.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        UUID encounterId = createResponse.getBody().getId();
        System.out.println("✅ Created encounter for retrieval test: " + encounterId);

        // When
        ResponseEntity<EncounterResponse> getResponse = restTemplate.getForEntity(
                getBaseUrl() + "/" + encounterId, EncounterResponse.class);

        // Then
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().getId()).isEqualTo(encounterId);
        assertThat(getResponse.getBody().getType()).isEqualTo(EncounterType.FOLLOW_UP);

        System.out.println("✅ Retrieved encounter successfully");
    }

    @Test
    void shouldReturn404ForNonExistentEncounter() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When
        ResponseEntity<Map> response = restTemplate.getForEntity(
                getBaseUrl() + "/" + nonExistentId, Map.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("code")).isEqualTo("ENCOUNTER_NOT_FOUND");

        System.out.println("✅ 404 error handling works correctly");
    }

    @Test
    void shouldHandleEncounterLifecycleViaHttp() {
        // Given - Create encounter
        CreateEncounterRequest request = new CreateEncounterRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                EncounterType.EMERGENCY,
                LocalDateTime.now(),
                BillingContext.GKV);

        ResponseEntity<EncounterResponse> createResponse = restTemplate.postForEntity(
                getBaseUrl(), request, EncounterResponse.class);
        UUID encounterId = createResponse.getBody().getId();

        System.out.println("✅ Created encounter for lifecycle test: " + encounterId);

        // When - Start encounter
        ResponseEntity<EncounterResponse> startResponse = restTemplate.exchange(
                getBaseUrl() + "/" + encounterId + "/start",
                HttpMethod.PUT, // Korrigiert: HttpMethod.PUT statt org.springframework.http.HttpMethod.PUT
                null,
                EncounterResponse.class);

        // Then
        assertThat(startResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(startResponse.getBody().getStatus()).isEqualTo(EncounterStatus.IN_PROGRESS);

        System.out.println("✅ Started encounter successfully");

        // When - Complete encounter
        ResponseEntity<EncounterResponse> completeResponse = restTemplate.exchange(
                getBaseUrl() + "/" + encounterId + "/complete",
                HttpMethod.PUT, // Korrigiert: HttpMethod.PUT
                null,
                EncounterResponse.class);

        // Then
        assertThat(completeResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(completeResponse.getBody().getStatus()).isEqualTo(EncounterStatus.COMPLETED);

        System.out.println("✅ Completed encounter successfully");
    }

    @Test
    void shouldValidateRequestFields() {
        // Given - Invalid request (missing required fields)
        CreateEncounterRequest invalidRequest = new CreateEncounterRequest();

        // When
        ResponseEntity<Map> response = restTemplate.postForEntity(
                getBaseUrl(), invalidRequest, Map.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("code")).isEqualTo("VALIDATION_ERROR");

        System.out.println("✅ Validation error handling works correctly");
    }
}
