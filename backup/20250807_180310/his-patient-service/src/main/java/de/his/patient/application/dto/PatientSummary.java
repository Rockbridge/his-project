// src/main/java/de/his/patient/application/dto/PatientSummary.java
package de.his.patient.application.dto;

import de.his.patient.domain.model.Gender;
import de.his.patient.domain.model.InsuranceStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Summary view of patient for lists")
public class PatientSummary {

    @Schema(description = "Patient ID")
    private UUID id;

    @Schema(description = "Patient's full name")
    private String fullName;

    @Schema(description = "Patient's birth date")
    private LocalDate birthDate;

    @Schema(description = "Patient's gender")
    private Gender gender;

    @Schema(description = "Krankenversichertennummer")
    private String kvnr;

    @Schema(description = "Insurance status")
    private InsuranceStatus insuranceStatus;

    @Schema(description = "Insurance company name")
    private String insuranceCompanyName;

    // Constructor
    public PatientSummary(UUID id, String fullName, LocalDate birthDate,
            Gender gender, String kvnr, InsuranceStatus insuranceStatus,
            String insuranceCompanyName) {
        this.id = id;
        this.fullName = fullName;
        this.birthDate = birthDate;
        this.gender = gender;
        this.kvnr = kvnr;
        this.insuranceStatus = insuranceStatus;
        this.insuranceCompanyName = insuranceCompanyName;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getKvnr() {
        return kvnr;
    }

    public void setKvnr(String kvnr) {
        this.kvnr = kvnr;
    }

    public InsuranceStatus getInsuranceStatus() {
        return insuranceStatus;
    }

    public void setInsuranceStatus(InsuranceStatus insuranceStatus) {
        this.insuranceStatus = insuranceStatus;
    }

    public String getInsuranceCompanyName() {
        return insuranceCompanyName;
    }

    public void setInsuranceCompanyName(String insuranceCompanyName) {
        this.insuranceCompanyName = insuranceCompanyName;
    }
}
