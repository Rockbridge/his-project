package de.his.encounter.infrastructure.client;

import de.his.encounter.infrastructure.client.dto.PatientDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "patient-service", url = "${services.patient.url:http://patient-service:8080}")
public interface PatientClient {

    @GetMapping("/api/v1/patients/{patientId}")
    PatientDto getPatient(@PathVariable UUID patientId);

    @GetMapping("/api/v1/patients/kvnr/{kvnr}")
    PatientDto getPatientByKvnr(@PathVariable String kvnr);
}