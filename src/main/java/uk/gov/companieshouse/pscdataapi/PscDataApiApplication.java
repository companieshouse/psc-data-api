package uk.gov.companieshouse.pscdataapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PscDataApiApplication {
    public static final String APPLICATION_NAME_SPACE = "psc-data-api";

    public static void main(String[] args) {
        SpringApplication.run(PscDataApiApplication.class, args);
    }

}