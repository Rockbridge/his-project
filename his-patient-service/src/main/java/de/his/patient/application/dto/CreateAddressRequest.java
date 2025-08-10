// src/main/java/de/his/patient/application/dto/CreateAddressRequest.java
package de.his.patient.application.dto;

import de.his.patient.domain.model.AddressType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request to create an address")
public class CreateAddressRequest {

    @NotNull(message = "Address type is required")
    @Schema(description = "Type of address")
    private AddressType addressType;

    @Schema(description = "Street name", example = "Musterstraße")
    private String street;

    @Schema(description = "House number", example = "123")
    private String houseNumber;

    @Schema(description = "Postal code", example = "80331")
    private String postalCode;

    @Schema(description = "City", example = "München")
    private String city;

    @Schema(description = "State", example = "Bayern")
    private String state;

    @Schema(description = "Country", example = "Deutschland")
    private String country;

    // Constructors
    public CreateAddressRequest() {
    }

    // Getters and Setters
    public AddressType getAddressType() {
        return addressType;
    }

    public void setAddressType(AddressType addressType) {
        this.addressType = addressType;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public void setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
