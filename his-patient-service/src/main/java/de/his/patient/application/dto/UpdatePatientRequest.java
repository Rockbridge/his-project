package de.his.patient.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;

@Schema(description = "Request to update patient information")
public class UpdatePatientRequest {

    @Schema(description = "Patient's first name", example = "Max")
    private String firstName;

    @Schema(description = "Patient's last name", example = "Mustermann")
    private String lastName;

    @Schema(description = "Patient's title", example = "Dr.")
    private String title;

    @Schema(description = "Phone number", example = "+49 89 12345678")
    private String phone;

    @Email(message = "Email must be valid")
    @Schema(description = "Email address", example = "max.mustermann@email.com")
    private String email;

    @Schema(description = "Consent for communication", example = "true")
    private Boolean consentCommunication;

    @Schema(description = "Consent for data processing", example = "true")
    private Boolean consentDataProcessing;

    public UpdatePatientRequest() {}

    // Getters and Setters
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Boolean getConsentCommunication() { return consentCommunication; }
    public void setConsentCommunication(Boolean consentCommunication) { this.consentCommunication = consentCommunication; }

    public Boolean getConsentDataProcessing() { return consentDataProcessing; }
    public void setConsentDataProcessing(Boolean consentDataProcessing) { this.consentDataProcessing = consentDataProcessing; }
}
