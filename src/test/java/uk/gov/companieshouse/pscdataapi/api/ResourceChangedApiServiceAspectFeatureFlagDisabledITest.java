package uk.gov.companieshouse.pscdataapi.api;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.chskafka.ChangedResource;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.chskafka.PrivateChangedResourceHandler;
import uk.gov.companieshouse.api.handler.chskafka.request.PrivateChangedResourcePost;
import uk.gov.companieshouse.api.http.HttpClient;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.sdk.ApiClientService;
import uk.gov.companieshouse.pscdataapi.util.TestHelper;

@SpringBootTest
class ResourceChangedApiServiceAspectFeatureFlagDisabledITest {

    @InjectMocks
    private ChsKafkaApiService chsKafkaApiService;

    @MockBean
    private ApiClientService apiClientService;

    @Mock
    private InternalApiClient internalApiClient;

    @Mock
    private ChangedResource changedResource;
    @Mock
    private PrivateChangedResourceHandler privateChangedResourceHandler;
    @Mock
    private PrivateChangedResourcePost changedResourcePost;
    @Mock
    private ApiResponse<Void> response;
    @Mock
    private HttpClient httpClient;

    @MockBean
    private ChsKafkaApiService mapper;

    private TestHelper testHelper;

    @Captor
    ArgumentCaptor<ChangedResource> changedResourceCaptor;



    @BeforeEach
    void setup() {
        when(internalApiClient.getHttpClient()).thenReturn(httpClient);
        testHelper = new TestHelper();
    }

    @Test
    void testThatKafkaApiShouldBeCalledWhenFeatureFlagDisabled()
            throws ApiErrorResponseException {

        when(internalApiClient.privateChangedResourceHandler()).thenReturn(
                privateChangedResourceHandler);
        when(privateChangedResourceHandler.postChangedResource(Mockito.any(), Mockito.any())).thenReturn(
                changedResourcePost);
        when(changedResourcePost.execute()).thenReturn(response);

        ApiResponse<?> apiResponse = chsKafkaApiService.invokeChsKafkaApi(TestHelper.X_REQUEST_ID, TestHelper.COMPANY_NUMBER, TestHelper.NOTIFICATION_ID, "kind");

        Assertions.assertThat(apiResponse).isNotNull();

        verify(internalApiClient).privateChangedResourceHandler();
        verify(privateChangedResourceHandler,times(1)).postChangedResource(Mockito.any(), changedResourceCaptor.capture());
        verify(changedResourcePost, times(1)).execute();
    }

    @Test
    void testThatKafkaApiShouldBeCalledOnDeleteWhenFeatureFlagDisabled()
            throws ApiErrorResponseException {

        when(internalApiClient.privateChangedResourceHandler()).thenReturn(
                privateChangedResourceHandler);
        when(privateChangedResourceHandler.postChangedResource(Mockito.any(), Mockito.any())).thenReturn(
                changedResourcePost);
        when(changedResourcePost.execute()).thenReturn(response);

        ApiResponse<?> apiResponse = chsKafkaApiService.invokeChsKafkaApiWithDeleteEvent(TestHelper.X_REQUEST_ID, TestHelper.COMPANY_NUMBER, TestHelper.NOTIFICATION_ID, "kind",
                TestHelper.buildPscDocument("individual-persons-with-significant-control").getData());

        Assertions.assertThat(apiResponse).isNotNull();

        verify(internalApiClient).privateChangedResourceHandler();
        verify(privateChangedResourceHandler,times(1)).postChangedResource(Mockito.any(), changedResourceCaptor.capture());
        verify(changedResourcePost, times(1)).execute();
    }
}
