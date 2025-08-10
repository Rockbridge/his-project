package de.his.encounter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class EncounterServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EncounterServiceApplication.class, args);
    }
}