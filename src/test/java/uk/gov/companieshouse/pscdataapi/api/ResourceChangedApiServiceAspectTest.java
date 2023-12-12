package uk.gov.companieshouse.pscdataapi.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNull;


@ExtendWith(MockitoExtension.class)
class ResourceChangedApiServiceAspectTest {

    @InjectMocks
    private ResourceChangedApiServiceAspect apiServiceAspect;

    @Test
    void testAspectDoesNotProceedWhenFlagDisabled() {
        // when
        Object actual = apiServiceAspect.checkStreamEventsEnabled();

        // then
        assertNull(actual);
    }
}