package de.his.encounter.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PatientDto {

    private UUID id;
    private String firstName;
    private String lastName;
    private String title;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    private String gender;
    private String kvnr;
    private String insuranceNumber;
    private String insuranceStatus;
    private String insuranceType;
    private String insuranceCompanyId;
    private String insuranceCompanyName;
    private String phone;
    private String email;
    private Boolean consentCommunication;
    private Boolean consentDataProcessing;

    // Flexibles DateTime-Format - nur ein Pattern
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime updatedAt;

    // Zusätzliches Feld vom Patient Service
    private String fullName;

    // Default constructor for Jackson
    public PatientDto() {
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getKvnr() {
        return kvnr;
    }

    public void setKvnr(String kvnr) {
        this.kvnr = kvnr;
    }

    public String getInsuranceNumber() {
        return insuranceNumber;
    }

    public void setInsuranceNumber(String insuranceNumber) {
        this.insuranceNumber = insuranceNumber;
    }

    public String getInsuranceStatus() {
        return insuranceStatus;
    }

    public void setInsuranceStatus(String insuranceStatus) {
        this.insuranceStatus = insuranceStatus;
    }

    public String getInsuranceType() {
        return insuranceType;
    }

    public void setInsuranceType(String insuranceType) {
        this.insuranceType = insuranceType;
    }

    public String getInsuranceCompanyId() {
        return insuranceCompanyId;
    }

    public void setInsuranceCompanyId(String insuranceCompanyId) {
        this.insuranceCompanyId = insuranceCompanyId;
    }

    public String getInsuranceCompanyName() {
        return insuranceCompanyName;
    }

    public void setInsuranceCompanyName(String insuranceCompanyName) {
        this.insuranceCompanyName = insuranceCompanyName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getConsentCommunication() {
        return consentCommunication;
    }

    public void setConsentCommunication(Boolean consentCommunication) {
        this.consentCommunication = consentCommunication;
    }

    public Boolean getConsentDataProcessing() {
        return consentDataProcessing;
    }

    public void setConsentDataProcessing(Boolean consentDataProcessing) {
        this.consentDataProcessing = consentDataProcessing;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    @Override
    public String toString() {
        return "PatientDto{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", kvnr='" + kvnr + '\'' +
                '}';
    }
}