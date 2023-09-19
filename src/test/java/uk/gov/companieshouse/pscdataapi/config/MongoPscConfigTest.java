package uk.gov.companieshouse.pscdataapi.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.nullValue;

@ExtendWith(MockitoExtension.class)
class MongoPscConfigTest {

    @Mock
    private MongoCustomConversions mongoCustomConversions;

    private MongoPscConfig mongoConfig;

    @BeforeEach
    void setUp() {
        mongoConfig = new MongoPscConfig();
    }

    @Test
    void mongoMappingContext() throws ClassNotFoundException {
        assertThat(mongoConfig.mongoMappingContext(mongoCustomConversions), is(not(nullValue())));
        assertThat(mongoConfig.mongoMappingContext(mongoCustomConversions), isA(MongoMappingContext.class));
    }
}
