package de.his.patient.presentation.controller;

import de.his.patient.application.dto.*;
import de.his.patient.application.service.PatientService;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/patients")
@Tag(name = "Patients", description = "Patient Management API")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @PostMapping
    @Operation(summary = "Create new patient", description = "Creates a new patient with VSDM-compliant data")
    public ResponseEntity<PatientResponse> createPatient(
            @Valid @RequestBody CreatePatientRequest request) {

        PatientResponse response = patientService.createPatient(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{patientId}")
    @Operation(summary = "Get patient details", description = "Retrieves detailed patient information")
    public ResponseEntity<PatientResponse> getPatient(
            @Parameter(description = "Patient ID") @PathVariable UUID patientId) {

        PatientResponse response = patientService.getPatient(patientId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/kvnr/{kvnr}")
    @Operation(summary = "Get patient by KVNR", description = "Retrieves patient by Krankenversichertennummer")
    public ResponseEntity<PatientResponse> getPatientByKvnr(
            @Parameter(description = "Krankenversichertennummer") @PathVariable String kvnr) {

        PatientResponse response = patientService.getPatientByKvnr(kvnr);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(summary = "Search patients", description = "Search patients by name or KVNR")
    public ResponseEntity<Page<PatientSummary>> searchPatients(
            @RequestParam String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<PatientSummary> patients = patientService.searchPatients(searchTerm, pageable);
        return ResponseEntity.ok(patients);
    }

    @DeleteMapping("/{patientId}")
    @Operation(summary = "Delete patient", description = "Soft deletes a patient")
    public ResponseEntity<Void> deletePatient(
            @Parameter(description = "Patient ID") @PathVariable UUID patientId) {

        patientService.deletePatient(patientId);
        return ResponseEntity.noContent().build();
    }
}
