package uk.gov.companieshouse.pscdataapi;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "uk.gov.companieshouse.pscdataapi.steps")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty,json:target/cucumber-report.json")
@SuppressWarnings("java:S2187") // Cucumber will run the scenarios, so no need for JUnit test methods in this class
public class CucumberFeaturesRunnerIT {



}