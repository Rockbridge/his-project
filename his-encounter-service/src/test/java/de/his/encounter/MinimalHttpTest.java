package de.his.encounter;

import de.his.encounter.application.dto.CreateEncounterRequest;
import de.his.encounter.domain.model.BillingContext;
import de.his.encounter.domain.model.EncounterType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class MinimalHttpTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldCreateEncounterOnly() {
        try {
            // Given
            String baseUrl = "http://localhost:" + port + "/api/v1/encounters";

            CreateEncounterRequest request = new CreateEncounterRequest(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    EncounterType.INITIAL,
                    LocalDateTime.now().plusDays(1),
                    BillingContext.GKV);

            System.out.println("🔍 Testing URL: " + baseUrl);
            System.out.println("🔍 Request: " + request.getType());

            // When
            ResponseEntity<String> response = restTemplate.postForEntity(
                    baseUrl, request, String.class);

            // Then
            System.out.println("📋 Response Status: " + response.getStatusCode());
            System.out.println("📋 Response Headers: " + response.getHeaders());
            System.out.println("📋 Response Body: " + response.getBody());

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("✅ Minimal HTTP test passed!");
            } else {
                System.err.println("❌ HTTP test failed with status: " + response.getStatusCode());
            }

        } catch (Exception e) {
            System.err.println("❌ Error in minimal HTTP test: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
