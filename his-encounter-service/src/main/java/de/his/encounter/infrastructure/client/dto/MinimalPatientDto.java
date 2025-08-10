package de.his.encounter.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MinimalPatientDto {

    private UUID id;
    private String firstName;
    private String lastName;
    private String kvnr;

    // Default constructor for Jackson
    public MinimalPatientDto() {
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

    public String getKvnr() {
        return kvnr;
    }

    public void setKvnr(String kvnr) {
        this.kvnr = kvnr;
    }

    @Override
    public String toString() {
        return "MinimalPatientDto{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", kvnr='" + kvnr + '\'' +
                '}';
    }
}