package uk.gov.companieshouse.pscdataapi;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import io.cucumber.spring.CucumberContextConfiguration;
import org.junit.runner.RunWith;
import uk.gov.companieshouse.pscdataapi.config.AbstractIntegrationTest;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/itest/resources/features",
        plugin = {"pretty", "json:target/cucumber-report.json"})
@CucumberContextConfiguration
public class CucumberFeaturesRunnerITest extends AbstractIntegrationTest {

}