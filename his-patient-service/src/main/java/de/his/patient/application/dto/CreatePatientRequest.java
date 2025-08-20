package de.his.patient.application.dto;

import de.his.patient.domain.model.Gender;
import de.his.patient.domain.model.InsuranceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Request to create a new patient")
public class CreatePatientRequest {

    @NotBlank(message = "First name is required")
    @Schema(description = "Patient's first name", example = "Max")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Schema(description = "Patient's last name", example = "Mustermann")
    private String lastName;

    @Schema(description = "Patient's title", example = "Dr.")
    private String title;

    @NotNull(message = "Birth date is required")
    @Schema(description = "Patient's birth date", example = "1985-03-15")
    private LocalDate birthDate;

    @NotNull(message = "Gender is required")
    @Schema(description = "Patient's gender")
    private Gender gender;

    @NotBlank(message = "KVNR is required")
    @Pattern(regexp = "^[A-Z][0-9]{9}$", message = "KVNR must be 1 letter followed by 9 digits")
    @Schema(description = "Krankenversichertennummer", example = "A123456789")
    private String kvnr;

    @Schema(description = "Insurance number", example = "1234567890")
    private String insuranceNumber;

    @Schema(description = "Type of insurance")
    private InsuranceType insuranceType;

    @Schema(description = "Insurance company ID", example = "104212059")
    private String insuranceCompanyId;

    @Schema(description = "Insurance company name", example = "AOK Bayern")
    private String insuranceCompanyName;

    @Schema(description = "Phone number", example = "+49 89 12345678")
    private String phone;

    @Email(message = "Email must be valid")
    @Schema(description = "Email address", example = "max.mustermann@email.com")
    private String email;

    @Schema(description = "Consent for communication", example = "true")
    private Boolean consentCommunication;

    @Schema(description = "Consent for data processing", example = "true")
    private Boolean consentDataProcessing;

    @Valid
    @Schema(description = "List of patient addresses")
    private List<CreateAddressRequest> addresses;

    public CreatePatientRequest() {
    }

    // Getters and Setters
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

    public String getInsuranceNumber() {
        return insuranceNumber;
    }

    public void setInsuranceNumber(String insuranceNumber) {
        this.insuranceNumber = insuranceNumber;
    }

    public InsuranceType getInsuranceType() {
        return insuranceType;
    }

    public void setInsuranceType(InsuranceType insuranceType) {
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

    public List<CreateAddressRequest> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<CreateAddressRequest> addresses) {
        this.addresses = addresses;
    }
}
