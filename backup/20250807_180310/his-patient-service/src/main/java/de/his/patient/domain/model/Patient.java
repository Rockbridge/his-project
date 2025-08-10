package de.his.patient.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

@Entity
@Table(name = "patients", indexes = {
        @Index(name = "idx_patient_kvnr", columnList = "kvnr", unique = true),
        @Index(name = "idx_patient_insurance_number", columnList = "insurance_number"),
})
public class Patient extends Person {

    @NotNull
    @Pattern(regexp = "^[A-Z][0-9]{9}$", message = "KVNR must be 1 letter followed by 9 digits")
    @Column(name = "kvnr", nullable = false, unique = true, length = 10)
    private String kvnr;

    @Column(name = "insurance_number", length = 20)
    private String insuranceNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "insurance_status", length = 50)
    private InsuranceStatus insuranceStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "insurance_type", length = 50)
    private InsuranceType insuranceType;

    @Column(name = "insurance_company_id")
    private String insuranceCompanyId;

    @Column(name = "insurance_company_name", length = 200)
    private String insuranceCompanyName;

    @Column(name = "consent_communication")
    private Boolean consentCommunication;

    @Column(name = "consent_data_processing")
    private Boolean consentDataProcessing;

    public Patient() {}

    public Patient(String firstName, String lastName, LocalDate birthDate, 
                   Gender gender, String kvnr) {
        super(firstName, lastName, birthDate, gender);
        this.kvnr = kvnr;
        this.insuranceStatus = InsuranceStatus.ACTIVE;
        this.consentCommunication = false;
        this.consentDataProcessing = false;
    }

    // Getters and Setters
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

    public Boolean getConsentCommunication() { return consentCommunication; }
    public void setConsentCommunication(Boolean consentCommunication) { this.consentCommunication = consentCommunication; }

    public Boolean getConsentDataProcessing() { return consentDataProcessing; }
    public void setConsentDataProcessing(Boolean consentDataProcessing) { this.consentDataProcessing = consentDataProcessing; }

    public boolean isInsuranceValid() {
        return insuranceStatus == InsuranceStatus.ACTIVE && 
               kvnr != null && !kvnr.isEmpty();
    }
}
