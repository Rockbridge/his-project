package de.his.encounter;

import de.his.encounter.application.dto.CreateEncounterRequest;
import de.his.encounter.application.dto.EncounterResponse;
import de.his.encounter.domain.model.BillingContext;
import de.his.encounter.domain.model.EncounterStatus;
import de.his.encounter.domain.model.EncounterType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class EncounterServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateAndRetrieveEncounter() throws Exception {
        // Given
        CreateEncounterRequest request = new CreateEncounterRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                EncounterType.INITIAL,
                LocalDateTime.now().plusDays(1),
                BillingContext.GKV);

        // When - Create encounter
        String createResponse = mockMvc.perform(post("/api/v1/encounters")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("INITIAL"))
                .andExpect(jsonPath("$.status").value("PLANNED"))
                .andReturn().getResponse().getContentAsString();

        EncounterResponse created = objectMapper.readValue(createResponse, EncounterResponse.class);

        // Then - Retrieve encounter
        mockMvc.perform(get("/api/v1/encounters/{id}", created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.getId().toString()))
                .andExpect(jsonPath("$.type").value("INITIAL"));
    }

    @Test
    void shouldHandleEncounterLifecycle() throws Exception {
        // Given - Create encounter
        CreateEncounterRequest request = new CreateEncounterRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                EncounterType.EMERGENCY,
                LocalDateTime.now(),
                BillingContext.GKV);

        String createResponse = mockMvc.perform(post("/api/v1/encounters")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        EncounterResponse created = objectMapper.readValue(createResponse, EncounterResponse.class);

        // When - Start encounter
        mockMvc.perform(put("/api/v1/encounters/{id}/start", created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        // Then - Complete encounter
        mockMvc.perform(put("/api/v1/encounters/{id}/complete", created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void shouldReturn404ForNonExistentEncounter() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/encounters/{id}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ENCOUNTER_NOT_FOUND"));
    }

    @Test
    void shouldValidateRequestFields() throws Exception {
        // Given - Invalid request
        CreateEncounterRequest invalidRequest = new CreateEncounterRequest();

        // When & Then
        mockMvc.perform(post("/api/v1/encounters")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
}
