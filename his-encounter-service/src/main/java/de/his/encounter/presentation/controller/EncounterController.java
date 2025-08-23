package de.his.encounter.presentation.controller;

import de.his.encounter.application.dto.CreateEncounterRequest;
import de.his.encounter.application.dto.EncounterResponse;
import de.his.encounter.application.dto.EncounterSummary;
import de.his.encounter.application.service.EncounterService;
import de.his.encounter.infrastructure.exception.InvalidPaginationParameterException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/encounters")
@Tag(name = "Encounters", description = "Encounter Management API")
public class EncounterController {

    private final EncounterService encounterService;

    public EncounterController(EncounterService encounterService) {
        this.encounterService = encounterService;
    }

    @PostMapping
    @Operation(summary = "Create new encounter", description = "Creates a new encounter for a patient")
    public ResponseEntity<EncounterResponse> createEncounter(
            @Valid @RequestBody CreateEncounterRequest request) {

        EncounterResponse response = encounterService.createEncounter(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/with-patient-validation")
    @Operation(summary = "Create encounter with patient validation", description = "Creates a new encounter with patient service validation")
    public ResponseEntity<EncounterResponse> createEncounterWithPatientValidation(
            @Valid @RequestBody CreateEncounterRequest request) {

        EncounterResponse response = encounterService.createEncounterWithPatientValidation(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{encounterId}")
    @Operation(summary = "Get encounter details", description = "Retrieves detailed encounter information")
    public ResponseEntity<EncounterResponse> getEncounter(
            @Parameter(description = "Encounter ID") @PathVariable UUID encounterId) {

        EncounterResponse response = encounterService.getEncounter(encounterId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Get patient encounters", description = "Retrieves encounters for a specific patient")
    public ResponseEntity<Page<EncounterSummary>> getPatientEncounters(
            @Parameter(description = "Patient ID") @PathVariable UUID patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) LocalDateTime fromDate,
            @RequestParam(required = false) LocalDateTime toDate) {
        if (page < 0) {
            throw new InvalidPaginationParameterException("page", page);
        }

        if (size <= 0) {
            throw new InvalidPaginationParameterException("size", size);
        }

        Pageable pageable = PageRequest.of(page, size);

        Page<EncounterSummary> encounters;
        if (fromDate != null && toDate != null) {
            encounters = encounterService.getPatientEncountersInDateRange(
                    patientId, fromDate, toDate, pageable);
        } else {
            encounters = encounterService.getPatientEncounters(patientId, pageable);
        }

        return ResponseEntity.ok(encounters);
    }

    @PutMapping("/{encounterId}/start")
    @Operation(summary = "Start encounter", description = "Changes encounter status to IN_PROGRESS")
    public ResponseEntity<EncounterResponse> startEncounter(
            @Parameter(description = "Encounter ID") @PathVariable UUID encounterId) {

        EncounterResponse response = encounterService.startEncounter(encounterId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{encounterId}/complete")
    @Operation(summary = "Complete encounter", description = "Changes encounter status to COMPLETED")
    public ResponseEntity<EncounterResponse> completeEncounter(
            @Parameter(description = "Encounter ID") @PathVariable UUID encounterId) {

        EncounterResponse response = encounterService.completeEncounter(encounterId);
        return ResponseEntity.ok(response);
    }
}
