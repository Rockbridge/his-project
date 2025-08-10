package de.his.patient.application.service;

import de.his.patient.domain.model.VSDMData;
import de.his.patient.domain.model.InsuranceStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class VSDMIntegrationService {

    private static final Logger logger = LoggerFactory.getLogger(VSDMIntegrationService.class);

    @Value("${vsdm.endpoint:https://vsdm.ti-dienste.de}")
    private String vsdmEndpoint;

    public VSDMData fetchVSDMData(String kvnr) {
        logger.info("Fetching VSDM data for KVNR {}", kvnr);
        
        try {
            // TODO: Implement actual VSDM API call
            // This is a placeholder implementation
            
            // For now, return mock data
            return new VSDMData(
                kvnr,
                InsuranceStatus.ACTIVE,
                "104212059",
                "AOK Bayern",
                "0" // No copayment
            );
            
        } catch (Exception e) {
            logger.error("Failed to fetch VSDM data for KVNR {}", kvnr, e);
            throw new RuntimeException("VSDM service unavailable", e);
        }
    }
}
