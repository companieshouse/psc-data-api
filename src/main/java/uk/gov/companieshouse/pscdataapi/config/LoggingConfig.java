package uk.gov.companieshouse.pscdataapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.pscdataapi.PscDataApiApplication;

/**
 * Configuration class for logging.
 */
@Configuration
public class LoggingConfig {

    /**
     * Creates a logger with specified namespace.
     *
     * @return the {@link LoggerFactory} for the specified namespace
     */
    @Bean
    public Logger logger() {
        return LoggerFactory.getLogger(PscDataApiApplication.APPLICATION_NAME_SPACE);
    }
}
