package de.his.patient.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "addresses")
public class Address extends AbstractEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;

    @Enumerated(EnumType.STRING)
    @Column(name = "address_type", nullable = false, length = 50)
    private AddressType addressType;

    @Column(name = "street", length = 200)
    private String street;

    @Column(name = "house_number", length = 10)
    private String houseNumber;

    @Column(name = "postal_code", length = 10)
    private String postalCode;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "country", length = 50)
    private String country;

    public Address() {}

    public Address(AddressType addressType, String street, String houseNumber,
                   String postalCode, String city, String country) {
        this.addressType = addressType;
        this.street = street;
        this.houseNumber = houseNumber;
        this.postalCode = postalCode;
        this.city = city;
        this.country = country;
    }

    // Getters and Setters
    public Person getPerson() { return person; }
    public void setPerson(Person person) { this.person = person; }

    public AddressType getAddressType() { return addressType; }
    public void setAddressType(AddressType addressType) { this.addressType = addressType; }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getHouseNumber() { return houseNumber; }
    public void setHouseNumber(String houseNumber) { this.houseNumber = houseNumber; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
}
