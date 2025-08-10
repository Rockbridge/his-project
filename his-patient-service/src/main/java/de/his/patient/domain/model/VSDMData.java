package de.his.patient.domain.model;

public class VSDMData {
    private final String kvnr;
    private final InsuranceStatus insuranceStatus;
    private final String insuranceCompanyId;
    private final String insuranceCompanyName;
    private final String copaymentStatus;

    public VSDMData(String kvnr, InsuranceStatus insuranceStatus, 
                    String insuranceCompanyId, String insuranceCompanyName,
                    String copaymentStatus) {
        this.kvnr = kvnr;
        this.insuranceStatus = insuranceStatus;
        this.insuranceCompanyId = insuranceCompanyId;
        this.insuranceCompanyName = insuranceCompanyName;
        this.copaymentStatus = copaymentStatus;
    }

    // Getters
    public String getKvnr() { return kvnr; }
    public InsuranceStatus getInsuranceStatus() { return insuranceStatus; }
    public String getInsuranceCompanyId() { return insuranceCompanyId; }
    public String getInsuranceCompanyName() { return insuranceCompanyName; }
    public String getCopaymentStatus() { return copaymentStatus; }
}
