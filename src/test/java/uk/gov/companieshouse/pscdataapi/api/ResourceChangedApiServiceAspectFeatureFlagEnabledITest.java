package uk.gov.companieshouse.pscdataapi.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.handler.chskafka.PrivateChangedResourceHandler;
import uk.gov.companieshouse.api.handler.chskafka.request.PrivateChangedResourcePost;
import uk.gov.companieshouse.api.sdk.ApiClientService;
import uk.gov.companieshouse.pscdataapi.exceptions.ServiceUnavailableException;
import uk.gov.companieshouse.pscdataapi.util.TestHelper;

import static org.mockito.Mockito.verifyNoInteractions;

@SpringBootTest
@ActiveProfiles("feature_flag_enabled")
class ResourceChangedApiServiceAspectFeatureFlagEnabledITest {
    @InjectMocks
    private ChsKafkaApiService chsKafkaApiService;

    @MockBean
    private ApiClientService apiClientService;

    @Mock
    private InternalApiClient internalApiClient;
    @Mock
    private PrivateChangedResourceHandler privateChangedResourceHandler;
    @Mock
    private PrivateChangedResourcePost changedResourcePost;

    private TestHelper testHelper;

    @BeforeEach
    void setup() {
        testHelper = new TestHelper();
    }
    @Test
    void testThatAspectShouldNotProceedWhenFeatureFlagEnabled() throws ServiceUnavailableException {

        chsKafkaApiService.invokeChsKafkaApi(testHelper.X_REQUEST_ID, testHelper.COMPANY_NUMBER, testHelper.NOTIFICATION_ID, "kind");

        //verifyNoInteractions(apiClientService);
        verifyNoInteractions(internalApiClient);
        verifyNoInteractions(privateChangedResourceHandler);
        verifyNoInteractions(changedResourcePost);
    }
}
