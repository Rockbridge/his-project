// src/main/java/de/his/patient/application/dto/AddressResponse.java
package de.his.patient.application.dto;

import de.his.patient.domain.model.AddressType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Address information")
public class AddressResponse {

    @Schema(description = "Address ID")
    private UUID id;

    @Schema(description = "Address type")
    private AddressType addressType;

    @Schema(description = "Street name")
    private String street;

    @Schema(description = "House number")
    private String houseNumber;

    @Schema(description = "Postal code")
    private String postalCode;

    @Schema(description = "City")
    private String city;

    @Schema(description = "State")
    private String state;

    @Schema(description = "Country")
    private String country;

    // Constructor
    public AddressResponse(UUID id, AddressType addressType, String street,
            String houseNumber, String postalCode, String city,
            String state, String country) {
        this.id = id;
        this.addressType = addressType;
        this.street = street;
        this.houseNumber = houseNumber;
        this.postalCode = postalCode;
        this.city = city;
        this.state = state;
        this.country = country;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

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

    public String getFullAddress() {
        StringBuilder fullAddress = new StringBuilder();
        if (street != null)
            fullAddress.append(street);
        if (houseNumber != null)
            fullAddress.append(" ").append(houseNumber);
        if (postalCode != null || city != null) {
            fullAddress.append(", ");
            if (postalCode != null)
                fullAddress.append(postalCode).append(" ");
            if (city != null)
                fullAddress.append(city);
        }
        return fullAddress.toString();
    }
}
