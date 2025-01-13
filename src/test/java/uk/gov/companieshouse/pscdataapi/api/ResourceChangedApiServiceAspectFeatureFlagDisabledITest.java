package uk.gov.companieshouse.pscdataapi.api;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.function.Supplier;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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
import uk.gov.companieshouse.pscdataapi.models.PscDeleteRequest;
import uk.gov.companieshouse.pscdataapi.util.TestHelper;

@SpringBootTest
class ResourceChangedApiServiceAspectFeatureFlagDisabledITest {

    @InjectMocks
    private ChsKafkaApiService chsKafkaApiService;

    @Captor
    ArgumentCaptor<ChangedResource> changedResourceCaptor;

    @MockBean
    private ApiClientService apiClientService;
    @MockBean
    private ChsKafkaApiService mapper;

    @Mock
    private Supplier<InternalApiClient> kafkaApiClientSupplier;
    @Mock
    private InternalApiClient client;
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
    @Mock
    private ObjectMapper objectMapper;

    @Test
    void testThatKafkaApiShouldBeCalledWhenFeatureFlagDisabled() throws ApiErrorResponseException {
        when(kafkaApiClientSupplier.get()).thenReturn(client);
        when(client.privateChangedResourceHandler()).thenReturn(
                privateChangedResourceHandler);
        when(privateChangedResourceHandler.postChangedResource(Mockito.any(), Mockito.any())).thenReturn(
                changedResourcePost);
        when(changedResourcePost.execute()).thenReturn(response);

        ApiResponse<?> apiResponse = chsKafkaApiService.invokeChsKafkaApi(TestHelper.X_REQUEST_ID, TestHelper.COMPANY_NUMBER, TestHelper.NOTIFICATION_ID, "individual-person-with-significant-control");

        Assertions.assertThat(apiResponse).isNotNull();

        verify(client).privateChangedResourceHandler();
        verify(privateChangedResourceHandler,times(1)).postChangedResource(Mockito.any(), changedResourceCaptor.capture());
        verify(changedResourcePost, times(1)).execute();
    }

    @Test
    void testThatKafkaApiShouldBeCalledOnDeleteWhenFeatureFlagDisabled() throws ApiErrorResponseException {
        when(kafkaApiClientSupplier.get()).thenReturn(client);
        when(client.privateChangedResourceHandler()).thenReturn(
                privateChangedResourceHandler);
        when(privateChangedResourceHandler.postChangedResource(Mockito.any(), Mockito.any())).thenReturn(
                changedResourcePost);
        when(changedResourcePost.execute()).thenReturn(response);

        ApiResponse<?> apiResponse = chsKafkaApiService.invokeChsKafkaApiWithDeleteEvent(
                new PscDeleteRequest(TestHelper.X_REQUEST_ID, TestHelper.COMPANY_NUMBER, TestHelper.NOTIFICATION_ID, "individual-person-with-significant-control", "deltaAt" ),
                TestHelper.buildPscDocument("individual-persons-with-significant-control"));

        Assertions.assertThat(apiResponse).isNotNull();

        verify(client).privateChangedResourceHandler();
        verify(privateChangedResourceHandler,times(1)).postChangedResource(Mockito.any(), changedResourceCaptor.capture());
        verify(changedResourcePost, times(1)).execute();
    }
}
