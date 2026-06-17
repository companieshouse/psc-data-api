package uk.gov.companieshouse.pscdataapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PscDataApiApplication {

    public static final String APPLICATION_NAME_SPACE = "psc-data-api";

    public static void main(String[] args) {
        SpringApplication.run(PscDataApiApplication.class, args);
    }

}