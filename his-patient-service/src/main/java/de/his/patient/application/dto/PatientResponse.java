package de.his.patient.application.dto;

import de.his.patient.domain.model.Gender;
import de.his.patient.domain.model.InsuranceStatus;
import de.his.patient.domain.model.InsuranceType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Complete patient information")
public class PatientResponse {

    @Schema(description = "Patient ID")
    private UUID id;

    @Schema(description = "Patient's first name")
    private String firstName;

    @Schema(description = "Patient's last name")
    private String lastName;

    @Schema(description = "Patient's title")
    private String title;

    @Schema(description = "Patient's birth date")
    private LocalDate birthDate;

    @Schema(description = "Patient's gender")
    private Gender gender;

    @Schema(description = "Krankenversichertennummer")
    private String kvnr;

    @Schema(description = "Insurance number")
    private String insuranceNumber;

    @Schema(description = "Insurance status")
    private InsuranceStatus insuranceStatus;

    @Schema(description = "Insurance type")
    private InsuranceType insuranceType;

    @Schema(description = "Insurance company ID")
    private String insuranceCompanyId;

    @Schema(description = "Insurance company name")
    private String insuranceCompanyName;

    @Schema(description = "Phone number")
    private String phone;

    @Schema(description = "Email address")
    private String email;

    @Schema(description = "Consent for communication")
    private Boolean consentCommunication;

    @Schema(description = "Consent for data processing")
    private Boolean consentDataProcessing;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    public PatientResponse() {}

    public PatientResponse(UUID id, String firstName, String lastName, String title,
                          LocalDate birthDate, Gender gender, String kvnr,
                          String insuranceNumber, InsuranceStatus insuranceStatus,
                          InsuranceType insuranceType, String insuranceCompanyId,
                          String insuranceCompanyName, String phone, String email,
                          Boolean consentCommunication, Boolean consentDataProcessing,
                          LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.title = title;
        this.birthDate = birthDate;
        this.gender = gender;
        this.kvnr = kvnr;
        this.insuranceNumber = insuranceNumber;
        this.insuranceStatus = insuranceStatus;
        this.insuranceType = insuranceType;
        this.insuranceCompanyId = insuranceCompanyId;
        this.insuranceCompanyName = insuranceCompanyName;
        this.phone = phone;
        this.email = email;
        this.consentCommunication = consentCommunication;
        this.consentDataProcessing = consentDataProcessing;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }

    public String getKvnr() { return kvnr; }
    public void setKvnr(String kvnr) { this.kvnr = kvnr; }

    public String getInsuranceNumber() { return insuranceNumber; }
    public void setInsuranceNumber(String insuranceNumber) { this.insuranceNumber = insuranceNumber; }

    public InsuranceStatus getInsuranceStatus() { return insuranceStatus; }
    public void setInsuranceStatus(InsuranceStatus insuranceStatus) { this.insuranceStatus = insuranceStatus; }

    public InsuranceType getInsuranceType() { return insuranceType; }
    public void setInsuranceType(InsuranceType insuranceType) { this.insuranceType = insuranceType; }

    public String getInsuranceCompanyId() { return insuranceCompanyId; }
    public void setInsuranceCompanyId(String insuranceCompanyId) { this.insuranceCompanyId = insuranceCompanyId; }

    public String getInsuranceCompanyName() { return insuranceCompanyName; }
    public void setInsuranceCompanyName(String insuranceCompanyName) { this.insuranceCompanyName = insuranceCompanyName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Boolean getConsentCommunication() { return consentCommunication; }
    public void setConsentCommunication(Boolean consentCommunication) { this.consentCommunication = consentCommunication; }

    public Boolean getConsentDataProcessing() { return consentDataProcessing; }
    public void setConsentDataProcessing(Boolean consentDataProcessing) { this.consentDataProcessing = consentDataProcessing; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getFullName() {
        StringBuilder fullName = new StringBuilder();
        if (title != null && !title.isEmpty()) {
            fullName.append(title).append(" ");
        }
        fullName.append(firstName).append(" ").append(lastName);
        return fullName.toString();
    }
}
