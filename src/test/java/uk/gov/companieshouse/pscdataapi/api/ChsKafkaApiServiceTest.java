package uk.gov.companieshouse.pscdataapi.api;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpResponseException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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

@ExtendWith(MockitoExtension.class)
class ChsKafkaApiServiceTest {

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
    @Mock
    private ObjectMapper objectMapper;
    @InjectMocks
    private ChsKafkaApiService chsKafkaApiService;
    @Captor
    ArgumentCaptor<ChangedResource> changedResourceCaptor;

    @Test
    void invokeChsKafkaEndpoint() throws ApiErrorResponseException {
        when(internalApiClient.privateChangedResourceHandler()).thenReturn(privateChangedResourceHandler);
        when(privateChangedResourceHandler.postChangedResource(any(), any())).thenReturn(privateChangedResourcePost);
        when(privateChangedResourcePost.execute()).thenReturn(response);

        ApiResponse<?> apiResponse = chsKafkaApiService.invokeChsKafkaApi(
                TestHelper.X_REQUEST_ID, TestHelper.COMPANY_NUMBER, TestHelper.NOTIFICATION_ID, "individual-person-with-significant-control");
        Assertions.assertThat(apiResponse).isNotNull();

        verify(internalApiClient, times(1)).privateChangedResourceHandler();
        verify(privateChangedResourceHandler, times(1)).postChangedResource(any(), changedResourceCaptor.capture());
        verify(privateChangedResourcePost, times(1)).execute();
        Assertions.assertThat(changedResourceCaptor.getValue().getEvent().getType()).isEqualTo("changed");
        Assertions.assertThat(changedResourceCaptor.getValue().getResourceUri()).isEqualTo("/company/companyNumber/persons-with-significant-control/individual/notificationId");
    }

    @Test
    void invokeChsKafkaEndpointDelete() throws ApiErrorResponseException {
        when(internalApiClient.privateChangedResourceHandler()).thenReturn(privateChangedResourceHandler);
        when(privateChangedResourceHandler.postChangedResource(any(), any())).thenReturn(privateChangedResourcePost);
        when(privateChangedResourcePost.execute()).thenReturn(response);

        ApiResponse<?> apiResponse = chsKafkaApiService.invokeChsKafkaApiWithDeleteEvent(
                TestHelper.X_REQUEST_ID, TestHelper.COMPANY_NUMBER, TestHelper.NOTIFICATION_ID, "kind",
                TestHelper.buildPscDocument("individual-persons-with-significant-control"));
        Assertions.assertThat(apiResponse).isNotNull();

        verify(internalApiClient, times(1)).privateChangedResourceHandler();
        verify(privateChangedResourceHandler, times(1)).postChangedResource(any(), changedResourceCaptor.capture());
        verify(privateChangedResourcePost, times(1)).execute();
        Assertions.assertThat(changedResourceCaptor.getValue().getEvent().getType()).isEqualTo("deleted");
    }

    @Test
    void invokeChsKafkaEndpointThrowsApiErrorException() throws ApiErrorResponseException {
        ApiErrorResponseException exception = new ApiErrorResponseException(new HttpResponseException.Builder(408, "Test Request timeout", new HttpHeaders()));
        when(internalApiClient.privateChangedResourceHandler()).thenReturn(privateChangedResourceHandler);
        when(privateChangedResourceHandler.postChangedResource(any(), any())).thenReturn(privateChangedResourcePost);
        when(privateChangedResourcePost.execute()).thenThrow(exception);

        Executable executable = () -> chsKafkaApiService.invokeChsKafkaApi(
                TestHelper.X_REQUEST_ID, TestHelper.COMPANY_NUMBER, TestHelper.NOTIFICATION_ID, "kind");

        assertThrows(ServiceUnavailableException.class, executable);
        verify(internalApiClient, times(1)).privateChangedResourceHandler();
        verify(privateChangedResourceHandler, times(1)).postChangedResource(any(), changedResourceCaptor.capture());
        verify(privateChangedResourcePost, times(1)).execute();
        Assertions.assertThat(changedResourceCaptor.getValue().getEvent().getType()).isEqualTo("changed");
    }

    @Test
    void invokeChsKafkaEndpointWithDeleteThrowsApiErrorException() throws ApiErrorResponseException {
        ApiErrorResponseException exception = new ApiErrorResponseException(new HttpResponseException.Builder(408, "Test Request timeout", new HttpHeaders()));
        when(internalApiClient.privateChangedResourceHandler()).thenReturn(privateChangedResourceHandler);
        when(privateChangedResourceHandler.postChangedResource(any(), any())).thenReturn(privateChangedResourcePost);
        when(privateChangedResourcePost.execute()).thenThrow(exception);

        Executable executable = () -> chsKafkaApiService.invokeChsKafkaApiWithDeleteEvent(
                TestHelper.X_REQUEST_ID, TestHelper.COMPANY_NUMBER, TestHelper.NOTIFICATION_ID, "kind", any());

        assertThrows(ServiceUnavailableException.class, executable);
        verify(internalApiClient, times(1)).privateChangedResourceHandler();
        verify(privateChangedResourceHandler, times(1)).postChangedResource(any(), changedResourceCaptor.capture());
        verify(privateChangedResourcePost, times(1)).execute();
        Assertions.assertThat(changedResourceCaptor.getValue().getEvent().getType()).isEqualTo("deleted");
    }

    @Test
    void invokeChsKafkaEndpointThrowsRuntimeException() throws ApiErrorResponseException {
        RuntimeException exception = new RuntimeException("Test Runtime exception");
        when(internalApiClient.privateChangedResourceHandler()).thenReturn(privateChangedResourceHandler);
        when(privateChangedResourceHandler.postChangedResource(any(), any())).thenReturn(privateChangedResourcePost);
        when(privateChangedResourcePost.execute()).thenThrow(exception);

        Executable executable = () -> chsKafkaApiService.invokeChsKafkaApi(
                TestHelper.X_REQUEST_ID, TestHelper.COMPANY_NUMBER, TestHelper.NOTIFICATION_ID, "kind");

        assertThrows(RuntimeException.class, executable);
        verify(internalApiClient, times(1)).privateChangedResourceHandler();
        verify(privateChangedResourceHandler, times(1)).postChangedResource(any(), changedResourceCaptor.capture());
        verify(privateChangedResourcePost, times(1)).execute();
        Assertions.assertThat(changedResourceCaptor.getValue().getEvent().getType()).isEqualTo("changed");
    }

    @Test
    void invokeChsKafkaEndpointWithDeleteThrowsRuntimeException() throws ApiErrorResponseException {
        RuntimeException exception = new RuntimeException("Test Runtime exception");
        when(internalApiClient.privateChangedResourceHandler()).thenReturn(privateChangedResourceHandler);
        when(privateChangedResourceHandler.postChangedResource(any(), any())).thenReturn(privateChangedResourcePost);
        when(privateChangedResourcePost.execute()).thenThrow(exception);

        Executable executable = () -> chsKafkaApiService.invokeChsKafkaApiWithDeleteEvent(
                TestHelper.X_REQUEST_ID, TestHelper.COMPANY_NUMBER, TestHelper.NOTIFICATION_ID, "kind", any());

        assertThrows(RuntimeException.class, executable);
        verify(internalApiClient, times(1)).privateChangedResourceHandler();
        verify(privateChangedResourceHandler, times(1)).postChangedResource(any(), changedResourceCaptor.capture());
        verify(privateChangedResourcePost, times(1)).execute();
        Assertions.assertThat(changedResourceCaptor.getValue().getEvent().getType()).isEqualTo("deleted");
    }
}
