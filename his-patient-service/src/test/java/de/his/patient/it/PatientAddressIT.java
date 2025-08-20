package de.his.patient.it;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test") // nutzt application-test.yml (siehe Schritt 2)
class PatientAddressIT {

    @Autowired
    MockMvc mvc;
    @Autowired
    DataSource ds;

    @Test
    void createsPatientWithAddress() throws Exception {
        String kvnr = "E" + (System.currentTimeMillis() % 1_000_000_000L);

        String body = """
                {
                  "kvnr":"%s",
                  "firstName":"Max",
                  "lastName":"Mustermann",
                  "birthDate":"1985-03-15",
                  "gender":"MALE",
                  "insuranceType":"STATUTORY",
                  "insuranceNumber":"123456789",
                  "consentDataProcessing":true,
                  "consentCommunication":false,
                  "addresses":[
                    {"addressType":"PRIMARY","street":"Hauptstraße","houseNumber":"42",
                     "postalCode":"48149","city":"Münster","state":"NRW","country":"Deutschland"}
                  ]
                }
                """.formatted(kvnr);

        mvc.perform(post("/api/v1/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Basic YWRtaW46ZGV2LXBhc3N3b3Jk") // admin:dev-password
                .content(body))
                .andExpect(status().isOk());

        try (Connection c = ds.getConnection();
                PreparedStatement ps = c.prepareStatement("""
                          SELECT COUNT(*) FROM his_patient.addresses a
                          JOIN his_patient.patients p ON p.id = a.person_id
                          WHERE p.kvnr = ?
                        """)) {
            ps.setString(1, kvnr);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                assertThat(rs.getInt(1)).isGreaterThanOrEqualTo(1);
            }
        }
    }
}
