package de.his.encounter.application.service;

import de.his.encounter.application.dto.CreateEncounterRequest;
import de.his.encounter.application.dto.EncounterResponse;
import de.his.encounter.application.dto.EncounterSummary;
import de.his.encounter.domain.model.Encounter;
import de.his.encounter.domain.model.BillingContext;
import de.his.encounter.domain.model.EncounterStatus;
import de.his.encounter.domain.model.EncounterType;
import de.his.encounter.domain.repository.EncounterRepository;
import de.his.encounter.infrastructure.exception.EncounterNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EncounterServiceTest {

    @Mock
    private EncounterRepository encounterRepository;

    @InjectMocks
    private EncounterService encounterService;

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

        Encounter savedEncounter = createTestEncounter();
        when(encounterRepository.save(any(Encounter.class))).thenReturn(savedEncounter);

        // When
        EncounterResponse response = encounterService.createEncounter(request);

        // Then
        assertThat(response.getId()).isEqualTo(encounterId);
        assertThat(response.getPatientId()).isEqualTo(patientId);
        assertThat(response.getType()).isEqualTo(EncounterType.INITIAL);
        assertThat(response.getStatus()).isEqualTo(EncounterStatus.PLANNED);
        assertThat(response.getBillingContext()).isEqualTo(BillingContext.GKV);

        verify(encounterRepository).save(any(Encounter.class));
    }

    @Test
    void shouldGetEncounter() {
        // Given
        Encounter encounter = createTestEncounter();
        when(encounterRepository.findById(encounterId)).thenReturn(Optional.of(encounter));

        // When
        EncounterResponse response = encounterService.getEncounter(encounterId);

        // Then
        assertThat(response.getId()).isEqualTo(encounterId);
        assertThat(response.getPatientId()).isEqualTo(patientId);
        verify(encounterRepository).findById(encounterId);
    }

    @Test
    void shouldThrowExceptionWhenEncounterNotFound() {
        // Given
        when(encounterRepository.findById(encounterId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> encounterService.getEncounter(encounterId))
                .isInstanceOf(EncounterNotFoundException.class)
                .hasMessageContaining(encounterId.toString());

        verify(encounterRepository).findById(encounterId);
    }

    @Test
    void shouldStartEncounter() {
        // Given
        Encounter encounter = createTestEncounter();
        when(encounterRepository.findById(encounterId)).thenReturn(Optional.of(encounter));
        when(encounterRepository.save(encounter)).thenReturn(encounter);

        // When
        EncounterResponse response = encounterService.startEncounter(encounterId);

        // Then
        assertThat(response.getStatus()).isEqualTo(EncounterStatus.IN_PROGRESS);
        verify(encounterRepository).findById(encounterId);
        verify(encounterRepository).save(encounter);
    }

    @Test
    void shouldCompleteEncounter() {
        // Given
        Encounter encounter = createTestEncounter();
        encounter.startEncounter(); // Erst starten, dann beenden
        when(encounterRepository.findById(encounterId)).thenReturn(Optional.of(encounter));
        when(encounterRepository.save(encounter)).thenReturn(encounter);

        // When
        EncounterResponse response = encounterService.completeEncounter(encounterId);

        // Then
        assertThat(response.getStatus()).isEqualTo(EncounterStatus.COMPLETED);
        verify(encounterRepository).findById(encounterId);
        verify(encounterRepository).save(encounter);
    }

    @Test
    void shouldGetPatientEncounters() {
        // Given
        Encounter encounter1 = createTestEncounter();
        Encounter encounter2 = createTestEncounter();
        Page<Encounter> encounterPage = new PageImpl<>(Arrays.asList(encounter1, encounter2));
        Pageable pageable = PageRequest.of(0, 10);

        when(encounterRepository.findByPatientIdOrderByEncounterDateDesc(patientId, pageable))
                .thenReturn(encounterPage);

        // When
        Page<EncounterSummary> result = encounterService.getPatientEncounters(patientId, pageable);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getId()).isEqualTo(encounterId);
        verify(encounterRepository).findByPatientIdOrderByEncounterDateDesc(patientId, pageable);
    }

    private Encounter createTestEncounter() {
        Encounter encounter = new Encounter(
                patientId, practitionerId, EncounterType.INITIAL,
                encounterDate, BillingContext.GKV);
        encounter.setId(encounterId);
        return encounter;
    }
}
