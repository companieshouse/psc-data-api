package uk.gov.companieshouse.pscdata;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import io.cucumber.spring.CucumberContextConfiguration;
import org.junit.runner.RunWith;
import org.springframework.test.context.TestPropertySource;

import uk.gov.companieshouse.pscdata.config.AbstractIntegrationTest;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/itest/resources/features",
        plugin = {"pretty", "json:target/cucumber-report.json"})
@CucumberContextConfiguration
public class CucumberFeaturesRunnerITest extends AbstractIntegrationTest {

}