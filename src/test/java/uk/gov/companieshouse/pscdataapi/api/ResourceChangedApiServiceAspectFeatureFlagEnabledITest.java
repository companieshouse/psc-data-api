package uk.gov.companieshouse.pscdataapi.api;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.chskafka.ChangedResource;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.chskafka.PrivateChangedResourceHandler;
import uk.gov.companieshouse.api.handler.chskafka.request.PrivateChangedResourcePost;
import uk.gov.companieshouse.api.http.HttpClient;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.sdk.ApiClientService;
import uk.gov.companieshouse.pscdataapi.util.TestHelper;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("feature_flag_enabled")
class ResourceChangedApiServiceAspectFeatureFlagEnabledITest {
//    @Autowired
//    private ResourceChangedApiService resourceChangedApiService;
//
//    @MockBean
//    private ApiClientService apiClientService;
//
//    @Mock
//    private InternalApiClient internalApiClient;
//    @Mock
//    private ResourceChangedRequest resourceChangedRequest;
//    @Mock
//    private PrivateChangedResourceHandler privateChangedResourceHandler;
//    @Mock
//    private PrivateChangedResourcePost changedResourcePost;
//
//    @Test
//    void testThatAspectShouldNotProceedWhenFeatureFlagEnabled() throws ServiceUnavailableException {
//
//        resourceChangedApiService.invokeChsKafkaApi(resourceChangedRequest);
//
//        verifyNoInteractions(apiClientService);
//        verifyNoInteractions(internalApiClient);
//        verifyNoInteractions(privateChangedResourceHandler);
//        verifyNoInteractions(changedResourcePost);
//    }
}
