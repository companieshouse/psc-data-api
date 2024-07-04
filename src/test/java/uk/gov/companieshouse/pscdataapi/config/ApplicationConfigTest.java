package uk.gov.companieshouse.pscdataapi.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsNot.not;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import uk.gov.companieshouse.api.InternalApiClient;

class ApplicationConfigTest {
    private ApplicationConfig applicationConfig;

    @BeforeEach
    void setUp() {
        applicationConfig = new ApplicationConfig();
    }
    @Test
    void mongoCustomConversions() {
        assertThat(applicationConfig.mongoCustomConversions(), is(not(nullValue())));
        assertThat(applicationConfig.mongoCustomConversions(), isA(MongoCustomConversions.class));
    }

    @Test
    void internalApiClient() {
        assertThat(applicationConfig.internalApiClient(), is(not(nullValue())));
        assertThat(applicationConfig.internalApiClient(), isA(InternalApiClient.class));
    }

}
