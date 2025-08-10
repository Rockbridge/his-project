package de.his.encounter.presentation.controller;

import de.his.encounter.application.dto.CreateEncounterRequest;
import de.his.encounter.application.dto.EncounterResponse;
import de.his.encounter.application.service.EncounterService;
import de.his.encounter.domain.model.BillingContext;
import de.his.encounter.domain.model.EncounterStatus;
import de.his.encounter.domain.model.EncounterType;
import de.his.encounter.infrastructure.exception.EncounterNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EncounterControllerTest {

    @Mock
    private EncounterService encounterService;

    @InjectMocks
    private EncounterController encounterController;

    private UUID patientId;
    private UUID practitionerId;
    private UUID encounterId;
    private LocalDateTime encounterDate;

    @BeforeEach
    void setUp() {
        patientId = UUID.randomUUID();
        practitionerId = UUID.randomUUID();
        encounterId = UUID.randomUUID();
        encounterDate = LocalDateTime.now().plusDays(1);
    }

    @Test
    void shouldCreateEncounter() {
        // Given
        CreateEncounterRequest request = new CreateEncounterRequest(
                patientId, practitionerId, EncounterType.INITIAL,
                encounterDate, BillingContext.GKV);

        EncounterResponse expectedResponse = new EncounterResponse(
                encounterId, patientId, practitionerId, EncounterType.INITIAL,
                encounterDate, EncounterStatus.PLANNED, BillingContext.GKV,
                Collections.emptyList(), LocalDateTime.now(), LocalDateTime.now());

        when(encounterService.createEncounter(any(CreateEncounterRequest.class)))
                .thenReturn(expectedResponse);

        // When
        ResponseEntity<EncounterResponse> response = encounterController.createEncounter(request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(encounterId);
        assertThat(response.getBody().getType()).isEqualTo(EncounterType.INITIAL);
        assertThat(response.getBody().getStatus()).isEqualTo(EncounterStatus.PLANNED);
    }

    @Test
    void shouldGetEncounter() {
        // Given
        EncounterResponse expectedResponse = new EncounterResponse(
                encounterId, patientId, practitionerId, EncounterType.FOLLOW_UP,
                encounterDate, EncounterStatus.PLANNED, BillingContext.PKV,
                Collections.emptyList(), LocalDateTime.now(), LocalDateTime.now());

        when(encounterService.getEncounter(encounterId)).thenReturn(expectedResponse);

        // When
        ResponseEntity<EncounterResponse> response = encounterController.getEncounter(encounterId);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(encounterId);
        assertThat(response.getBody().getType()).isEqualTo(EncounterType.FOLLOW_UP);
    }

    @Test
    void shouldStartEncounter() {
        // Given
        EncounterResponse expectedResponse = new EncounterResponse(
                encounterId, patientId, practitionerId, EncounterType.INITIAL,
                encounterDate, EncounterStatus.IN_PROGRESS, BillingContext.GKV,
                Collections.emptyList(), LocalDateTime.now(), LocalDateTime.now());

        when(encounterService.startEncounter(encounterId)).thenReturn(expectedResponse);

        // When
        ResponseEntity<EncounterResponse> response = encounterController.startEncounter(encounterId);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(EncounterStatus.IN_PROGRESS);
    }

    @Test
    void shouldCompleteEncounter() {
        // Given
        EncounterResponse expectedResponse = new EncounterResponse(
                encounterId, patientId, practitionerId, EncounterType.INITIAL,
                encounterDate, EncounterStatus.COMPLETED, BillingContext.GKV,
                Collections.emptyList(), LocalDateTime.now(), LocalDateTime.now());

        when(encounterService.completeEncounter(encounterId)).thenReturn(expectedResponse);

        // When
        ResponseEntity<EncounterResponse> response = encounterController.completeEncounter(encounterId);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(EncounterStatus.COMPLETED);
    }
}
