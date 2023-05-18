package uk.gov.companieshouse.pscdataapi.api;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpResponseException;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.chskafka.ChangedResource;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.chskafka.PrivateChangedResourceHandler;
import uk.gov.companieshouse.api.handler.chskafka.request.PrivateChangedResourcePost;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscdataapi.exceptions.ServiceUnavailableException;
import uk.gov.companieshouse.pscdataapi.util.TestHelper;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ChsKafkaApiServiceTest {

    @Mock
    private Logger logger;
    @Mock
    InternalApiClient internalApiClient;
    @Mock
    PrivateChangedResourceHandler privateChangedResourceHandler;
    @Mock
    private PrivateChangedResourcePost privateChangedResourcePost;
    @Mock
    private ApiResponse<Void> response;

    private TestHelper testHelper;
    @InjectMocks
    private ChsKafkaApiService chsKafkaApiService;

    @Captor
    ArgumentCaptor<ChangedResource> changedResourceCaptor;

    @BeforeEach
    void setUp() {
        testHelper = new TestHelper();
    }

    @Test
    void invokeChsKafkaEndpoint() throws ApiErrorResponseException {
        when(internalApiClient.privateChangedResourceHandler()).thenReturn(privateChangedResourceHandler);
        when(privateChangedResourceHandler.postChangedResource(Mockito.any(), Mockito.any())).thenReturn(privateChangedResourcePost);
        when(privateChangedResourcePost.execute()).thenReturn(response);

        ApiResponse<?> apiResponse = chsKafkaApiService.invokeChsKafkaApi(
                TestHelper.X_REQUEST_ID, TestHelper.COMPANY_NUMBER, TestHelper.NOTIFICATION_ID, "kind");
        Assertions.assertThat(apiResponse).isNotNull();

        verify(internalApiClient, times(1)).privateChangedResourceHandler();
        verify(privateChangedResourceHandler, times(1)).postChangedResource(Mockito.any(), changedResourceCaptor.capture());
        verify(privateChangedResourcePost, times(1)).execute();
        Assertions.assertThat(changedResourceCaptor.getValue()).isNotNull();
    }

    @Test
    void invokeChsKafkaEndpointThrowsApiErrorException() throws ApiErrorResponseException {
        ApiErrorResponseException exception = new ApiErrorResponseException(new HttpResponseException.Builder(408, "Test Request timeout", new HttpHeaders()));
        when(internalApiClient.privateChangedResourceHandler()).thenReturn(privateChangedResourceHandler);
        when(privateChangedResourceHandler.postChangedResource(Mockito.any(), Mockito.any())).thenReturn(privateChangedResourcePost);
        when(privateChangedResourcePost.execute()).thenThrow(exception);

        Assert.assertThrows(ServiceUnavailableException.class, () -> chsKafkaApiService.invokeChsKafkaApi(
                TestHelper.X_REQUEST_ID, TestHelper.COMPANY_NUMBER, TestHelper.NOTIFICATION_ID, "kind"));

        verify(internalApiClient, times(1)).privateChangedResourceHandler();
        verify(privateChangedResourceHandler, times(1)).postChangedResource(Mockito.any(), changedResourceCaptor.capture());
        verify(privateChangedResourcePost, times(1)).execute();
        Assertions.assertThat(changedResourceCaptor.getValue()).isNotNull();
    }

    @Test
    void invokeChsKafkaEndpointThrowsRuntimeException() throws ApiErrorResponseException {
        RuntimeException exception = new RuntimeException("Test Runtime exception");
        when(internalApiClient.privateChangedResourceHandler()).thenReturn(privateChangedResourceHandler);
        when(privateChangedResourceHandler.postChangedResource(Mockito.any(), Mockito.any())).thenReturn(privateChangedResourcePost);
        when(privateChangedResourcePost.execute()).thenThrow(exception);

        Assert.assertThrows(RuntimeException.class, () -> chsKafkaApiService.invokeChsKafkaApi(
                TestHelper.X_REQUEST_ID, TestHelper.COMPANY_NUMBER, TestHelper.NOTIFICATION_ID, "kind"));

        verify(internalApiClient, times(1)).privateChangedResourceHandler();
        verify(privateChangedResourceHandler, times(1)).postChangedResource(Mockito.any(), changedResourceCaptor.capture());
        verify(privateChangedResourcePost, times(1)).execute();
        Assertions.assertThat(changedResourceCaptor.getValue()).isNotNull();
    }
}
