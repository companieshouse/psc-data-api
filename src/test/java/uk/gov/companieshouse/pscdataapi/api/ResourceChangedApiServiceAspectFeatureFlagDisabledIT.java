package uk.gov.companieshouse.pscdataapi.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.chskafka.ChangedResource;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.chskafka.PrivateChangedResourceHandler;
import uk.gov.companieshouse.api.handler.chskafka.request.PrivateChangedResourcePost;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.psc.Individual;
import uk.gov.companieshouse.api.sdk.ApiClientService;
import uk.gov.companieshouse.pscdataapi.models.PscDeleteRequest;
import uk.gov.companieshouse.pscdataapi.transform.CompanyPscTransformer;
import uk.gov.companieshouse.pscdataapi.util.TestHelper;

@SpringBootTest
class ResourceChangedApiServiceAspectFeatureFlagDisabledIT {

    @InjectMocks
    private ChsKafkaApiService chsKafkaApiService;

    @Captor
    ArgumentCaptor<ChangedResource> changedResourceCaptor;

    @MockitoBean
    private ApiClientService apiClientService;

    @Mock
    private Supplier<InternalApiClient> kafkaApiClientSupplier;
    @Mock
    private InternalApiClient client;
    @Mock
    private PrivateChangedResourceHandler privateChangedResourceHandler;
    @Mock
    private PrivateChangedResourcePost changedResourcePost;
    @Mock
    private ApiResponse<Void> response;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private CompanyPscTransformer companyPscTransformer;

    @Test
    void testThatKafkaApiShouldBeCalledWhenFeatureFlagDisabled() throws ApiErrorResponseException {
        when(kafkaApiClientSupplier.get()).thenReturn(client);
        when(client.privateChangedResourceHandler()).thenReturn(
                privateChangedResourceHandler);
        when(privateChangedResourceHandler.postChangedResource(Mockito.any(), Mockito.any())).thenReturn(
                changedResourcePost);
        when(changedResourcePost.execute()).thenReturn(response);

        ApiResponse<?> apiResponse = chsKafkaApiService.invokeChsKafkaApi(TestHelper.COMPANY_NUMBER, TestHelper.NOTIFICATION_ID,
                "individual-person-with-significant-control");

        assertThat(apiResponse).isNotNull();

        verify(client).privateChangedResourceHandler();
        verify(privateChangedResourceHandler, times(1)).postChangedResource(Mockito.any(), changedResourceCaptor.capture());
        verify(changedResourcePost, times(1)).execute();
    }

    @Test
    void testThatKafkaApiShouldBeCalledOnDeleteWhenFeatureFlagDisabled() throws ApiErrorResponseException, JsonProcessingException {
        when(kafkaApiClientSupplier.get()).thenReturn(client);
        when(client.privateChangedResourceHandler()).thenReturn(
                privateChangedResourceHandler);
        when(privateChangedResourceHandler.postChangedResource(Mockito.any(), Mockito.any())).thenReturn(
                changedResourcePost);
        when(changedResourcePost.execute()).thenReturn(response);

        Individual individual = new Individual();
        individual.setKind(Individual.KindEnum.INDIVIDUAL_PERSON_WITH_SIGNIFICANT_CONTROL);
        when(companyPscTransformer.transformPscDocToIndividual(any(), eq(false))).thenReturn(individual);
        when(objectMapper.writeValueAsString(individual)).thenReturn(individual.toString());
        when(objectMapper.readValue(individual.toString(), Object.class)).thenReturn(individual);

        ApiResponse<?> apiResponse = chsKafkaApiService.invokeChsKafkaApiWithDeleteEvent(
                new PscDeleteRequest(TestHelper.X_REQUEST_ID, TestHelper.COMPANY_NUMBER, TestHelper.NOTIFICATION_ID,
                        "individual-person-with-significant-control", "deltaAt"),
                TestHelper.buildPscDocument("individual-person-with-significant-control"));

        assertThat(apiResponse).isNotNull();

        verify(client).privateChangedResourceHandler();
        verify(privateChangedResourceHandler, times(1)).postChangedResource(Mockito.any(), changedResourceCaptor.capture());
        verify(changedResourcePost, times(1)).execute();
        verify(companyPscTransformer, times(2)).transformPscDocToIndividual(any(), eq(false));

        ChangedResource captured = changedResourceCaptor.getValue();
        assertThat(captured.getEvent().getType()).isEqualTo("deleted");
        assertThat(captured.getDeletedData()).isInstanceOf(Individual.class);
        assertThat(captured.getResourceKind()).isEqualTo("company-psc-individual");
    }
}
