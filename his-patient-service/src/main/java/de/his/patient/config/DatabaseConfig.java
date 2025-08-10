package de.his.patient.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "de.his.patient.domain.repository")
@EnableTransactionManagement
public class DatabaseConfig {
}
