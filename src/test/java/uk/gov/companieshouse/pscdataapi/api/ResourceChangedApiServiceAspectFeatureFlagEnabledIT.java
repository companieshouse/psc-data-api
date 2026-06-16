package uk.gov.companieshouse.pscdataapi.api;

import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.chskafka.ChangedResource;
import uk.gov.companieshouse.api.handler.chskafka.PrivateChangedResourceHandler;
import uk.gov.companieshouse.api.handler.chskafka.request.PrivateChangedResourcePost;
import uk.gov.companieshouse.api.http.HttpClient;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.request.RequestExecutor;
import uk.gov.companieshouse.api.sdk.ApiClientService;
import uk.gov.companieshouse.pscdataapi.exceptions.ServiceUnavailableException;
import uk.gov.companieshouse.pscdataapi.models.PscDeleteRequest;
import uk.gov.companieshouse.pscdataapi.util.TestHelper;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(properties = {"feature.seeding_collection_enabled=true"})
class ResourceChangedApiServiceAspectFeatureFlagEnabledIT {

    @Autowired
    private ChsKafkaApiService chsKafkaApiService;

    @MockitoBean
    private ApiClientService apiClientService;

    @Mock
    private InternalApiClient internalApiClient;
    @Mock
    private PrivateChangedResourceHandler privateChangedResourceHandler;
    @Mock
    private PrivateChangedResourcePost changedResourcePost;
    @Mock
    private ApiResponse<Void> response;
    @Mock
    private HttpClient httpClient;

    @Mock
    private ChangedResource changedResource;

    @Mock
    private RequestExecutor requestExecutor;

    @Test
    void testThatAspectShouldNotProceedWhenFeatureFlagEnabled() throws ServiceUnavailableException {

        chsKafkaApiService.invokeChsKafkaApi(TestHelper.COMPANY_NUMBER, TestHelper.NOTIFICATION_ID,
                "individual-person-with-significant-control");

        verifyNoInteractions(apiClientService);
        verifyNoInteractions(internalApiClient);
        verifyNoInteractions(privateChangedResourceHandler);
        verifyNoInteractions(changedResourcePost);
    }

    @Test
    void testThatAspectShouldNotProceedOnDeleteWhenFeatureFlagEnabled()
            throws ServiceUnavailableException {

        chsKafkaApiService.invokeChsKafkaApiWithDeleteEvent(
                new PscDeleteRequest(TestHelper.X_REQUEST_ID, TestHelper.COMPANY_NUMBER, TestHelper.NOTIFICATION_ID,
                        "individual-person-with-significant-control", "deltaAt"),
                TestHelper.buildPscDocument("individual-persons-with-significant-control"));

        verifyNoInteractions(apiClientService);
        verifyNoInteractions(internalApiClient);
        verifyNoInteractions(privateChangedResourceHandler);
        verifyNoInteractions(changedResourcePost);
    }
}
