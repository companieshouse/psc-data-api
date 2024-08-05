package uk.gov.companieshouse.pscdataapi.api;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import uk.gov.companieshouse.api.psc.*;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscdataapi.exceptions.ServiceUnavailableException;
import uk.gov.companieshouse.pscdataapi.models.PscDocument;
import uk.gov.companieshouse.pscdataapi.transform.CompanyPscTransformer;
import uk.gov.companieshouse.pscdataapi.util.TestHelper;

@ExtendWith(MockitoExtension.class)
class ChsKafkaApiServiceTest {

    private static final String EVENT_TYPE_CHANGED = "changed";
    private static final String EVENT_TYPE_DELETED = "deleted";

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
    @Mock
    CompanyPscTransformer companyPscTransformer;
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
                TestHelper.X_REQUEST_ID, TestHelper.COMPANY_NUMBER, TestHelper.NOTIFICATION_ID, TestHelper.INDIVIDUAL_KIND);
        Assertions.assertThat(apiResponse).isNotNull();

        verify(internalApiClient, times(1)).privateChangedResourceHandler();
        verify(privateChangedResourceHandler, times(1)).postChangedResource(any(), changedResourceCaptor.capture());
        verify(privateChangedResourcePost, times(1)).execute();
        Assertions.assertThat(changedResourceCaptor.getValue().getEvent().getType()).isEqualTo(EVENT_TYPE_CHANGED);
        Assertions.assertThat(changedResourceCaptor.getValue().getResourceUri()).isEqualTo("/company/companyNumber/persons-with-significant-control/individual/notificationId");
    }

    @Test
    void invokeChsKafkaEndpointWithDeleteForIndividual() throws ApiErrorResponseException, JsonProcessingException {
        // given
        when(internalApiClient.privateChangedResourceHandler()).thenReturn(privateChangedResourceHandler);
        when(privateChangedResourceHandler.postChangedResource(any(), any())).thenReturn(privateChangedResourcePost);
        when(privateChangedResourcePost.execute()).thenReturn(response);

        PscDocument document = TestHelper.buildPscDocument(TestHelper.INDIVIDUAL_KIND);
        Individual individual = new Individual();
        individual.setKind(Individual.KindEnum.INDIVIDUAL_PERSON_WITH_SIGNIFICANT_CONTROL);
        individual.setName("Individual");
        when(companyPscTransformer.transformPscDocToIndividual(document, false)).thenReturn(individual);

        when(objectMapper.writeValueAsString(individual)).thenReturn(individual.toString());
        when(objectMapper.readValue(individual.toString(), Object.class)).thenReturn(individual);

        // when
        ApiResponse<?> apiResponse = chsKafkaApiService.invokeChsKafkaApiWithDeleteEvent(
                TestHelper.X_REQUEST_ID, TestHelper.COMPANY_NUMBER, TestHelper.NOTIFICATION_ID, TestHelper.INDIVIDUAL_KIND,
                document);
        Assertions.assertThat(apiResponse).isNotNull();

        //then
        verify(internalApiClient, times(1)).privateChangedResourceHandler();
        verify(privateChangedResourceHandler, times(1)).postChangedResource(any(), changedResourceCaptor.capture());
        verify(privateChangedResourcePost, times(1)).execute();
        Assertions.assertThat(changedResourceCaptor.getValue().getEvent().getType()).isEqualTo(EVENT_TYPE_DELETED);
        Assertions.assertThat(changedResourceCaptor.getValue().getDeletedData()).isInstanceOf(Individual.class);
    }

    @Test
    void invokeChsKafkaEndpointWithDeleteForIndividualBeneficialOwner() throws ApiErrorResponseException, JsonProcessingException {
        // given
        when(internalApiClient.privateChangedResourceHandler()).thenReturn(privateChangedResourceHandler);
        when(privateChangedResourceHandler.postChangedResource(any(), any())).thenReturn(privateChangedResourcePost);
        when(privateChangedResourcePost.execute()).thenReturn(response);

        PscDocument document = TestHelper.buildPscDocument(TestHelper.INDIVIDUAL_BO_KIND);
        IndividualBeneficialOwner individualBO = new IndividualBeneficialOwner();
        individualBO.setKind(IndividualBeneficialOwner.KindEnum.INDIVIDUAL_BENEFICIAL_OWNER);
        individualBO.setName("Individual-Beneficial-Owner");
        when(companyPscTransformer.transformPscDocToIndividualBeneficialOwner(document, false)).thenReturn(individualBO);

        when(objectMapper.writeValueAsString(individualBO)).thenReturn(individualBO.toString());
        when(objectMapper.readValue(individualBO.toString(), Object.class)).thenReturn(individualBO);

        // when
        ApiResponse<?> apiResponse = chsKafkaApiService.invokeChsKafkaApiWithDeleteEvent(
                TestHelper.X_REQUEST_ID, TestHelper.COMPANY_NUMBER, TestHelper.NOTIFICATION_ID, TestHelper.INDIVIDUAL_KIND,
                document);
        Assertions.assertThat(apiResponse).isNotNull();

        //then
        verify(internalApiClient, times(1)).privateChangedResourceHandler();
        verify(privateChangedResourceHandler, times(1)).postChangedResource(any(), changedResourceCaptor.capture());
        verify(privateChangedResourcePost, times(1)).execute();
        Assertions.assertThat(changedResourceCaptor.getValue().getEvent().getType()).isEqualTo(EVENT_TYPE_DELETED);
        Assertions.assertThat(changedResourceCaptor.getValue().getDeletedData()).isInstanceOf(IndividualBeneficialOwner.class);
    }

    @Test
    void invokeChsKafkaEndpointWithDeleteForLegalPerson() throws ApiErrorResponseException, JsonProcessingException {
        // given
        when(internalApiClient.privateChangedResourceHandler()).thenReturn(privateChangedResourceHandler);
        when(privateChangedResourceHandler.postChangedResource(any(), any())).thenReturn(privateChangedResourcePost);
        when(privateChangedResourcePost.execute()).thenReturn(response);

        PscDocument document = TestHelper.buildPscDocument(TestHelper.LEGAL_KIND);
        LegalPerson legalPerson = new LegalPerson();
        legalPerson.setKind(LegalPerson.KindEnum.LEGAL_PERSON_PERSON_WITH_SIGNIFICANT_CONTROL);
        legalPerson.setName("Legal-Person");
        when(companyPscTransformer.transformPscDocToLegalPerson(document)).thenReturn(legalPerson);

        when(objectMapper.writeValueAsString(legalPerson)).thenReturn(legalPerson.toString());
        when(objectMapper.readValue(legalPerson.toString(), Object.class)).thenReturn(legalPerson);

        // when
        ApiResponse<?> apiResponse = chsKafkaApiService.invokeChsKafkaApiWithDeleteEvent(
                TestHelper.X_REQUEST_ID, TestHelper.COMPANY_NUMBER, TestHelper.NOTIFICATION_ID, TestHelper.LEGAL_KIND,
                document);
        Assertions.assertThat(apiResponse).isNotNull();

        //then
        verify(internalApiClient, times(1)).privateChangedResourceHandler();
        verify(privateChangedResourceHandler, times(1)).postChangedResource(any(), changedResourceCaptor.capture());
        verify(privateChangedResourcePost, times(1)).execute();
        Assertions.assertThat(changedResourceCaptor.getValue().getEvent().getType()).isEqualTo(EVENT_TYPE_DELETED);
        Assertions.assertThat(changedResourceCaptor.getValue().getDeletedData()).isInstanceOf(LegalPerson.class);
    }

    @Test
    void invokeChsKafkaEndpointWithDeleteForLegalPersonBeneficialOwner() throws ApiErrorResponseException, JsonProcessingException {
        // given
        when(internalApiClient.privateChangedResourceHandler()).thenReturn(privateChangedResourceHandler);
        when(privateChangedResourceHandler.postChangedResource(any(), any())).thenReturn(privateChangedResourcePost);
        when(privateChangedResourcePost.execute()).thenReturn(response);

        PscDocument document = TestHelper.buildPscDocument(TestHelper.LEGAL_BO_KIND);
        LegalPersonBeneficialOwner legalPersonBeneficialOwner = new LegalPersonBeneficialOwner();
        legalPersonBeneficialOwner.setKind(LegalPersonBeneficialOwner.KindEnum.LEGAL_PERSON_BENEFICIAL_OWNER);
        legalPersonBeneficialOwner.setName("Legal-Person-Beneficial-Owner");
        when(companyPscTransformer.transformPscDocToLegalPersonBeneficialOwner(
                document)).thenReturn(legalPersonBeneficialOwner);

        when(objectMapper.writeValueAsString(legalPersonBeneficialOwner))
                .thenReturn(legalPersonBeneficialOwner.toString());
        when(objectMapper.readValue(legalPersonBeneficialOwner.toString(), Object.class))
                .thenReturn(legalPersonBeneficialOwner);

        // when
        ApiResponse<?> apiResponse = chsKafkaApiService.invokeChsKafkaApiWithDeleteEvent(
                TestHelper.X_REQUEST_ID, TestHelper.COMPANY_NUMBER, TestHelper.NOTIFICATION_ID, TestHelper.LEGAL_BO_KIND,
                document);
        Assertions.assertThat(apiResponse).isNotNull();

        //then
        verify(internalApiClient, times(1)).privateChangedResourceHandler();
        verify(privateChangedResourceHandler, times(1)).postChangedResource(any(), changedResourceCaptor.capture());
        verify(privateChangedResourcePost, times(1)).execute();
        Assertions.assertThat(changedResourceCaptor.getValue().getEvent().getType()).isEqualTo(EVENT_TYPE_DELETED);
        Assertions.assertThat(changedResourceCaptor.getValue().getDeletedData()).isInstanceOf(LegalPersonBeneficialOwner.class);
    }

    @Test
    void invokeChsKafkaEndpointWithDeleteForSuperSecure() throws ApiErrorResponseException, JsonProcessingException {
        // given
        when(internalApiClient.privateChangedResourceHandler()).thenReturn(privateChangedResourceHandler);
        when(privateChangedResourceHandler.postChangedResource(any(), any())).thenReturn(privateChangedResourcePost);
        when(privateChangedResourcePost.execute()).thenReturn(response);

        PscDocument document = TestHelper.buildPscDocument(TestHelper.SECURE_KIND);
        SuperSecure superSecure = new SuperSecure();
        superSecure.setKind(SuperSecure.KindEnum.SUPER_SECURE_PERSON_WITH_SIGNIFICANT_CONTROL);
        when(companyPscTransformer.transformPscDocToSuperSecure(
                document)).thenReturn(superSecure);

        when(objectMapper.writeValueAsString(superSecure)).thenReturn(superSecure.toString());
        when(objectMapper.readValue(superSecure.toString(), Object.class)).thenReturn(superSecure);

        // when
        ApiResponse<?> apiResponse = chsKafkaApiService.invokeChsKafkaApiWithDeleteEvent(
                TestHelper.X_REQUEST_ID, TestHelper.COMPANY_NUMBER, TestHelper.NOTIFICATION_ID, TestHelper.SECURE_KIND,
                document);
        Assertions.assertThat(apiResponse).isNotNull();

        //then
        verify(internalApiClient, times(1)).privateChangedResourceHandler();
        verify(privateChangedResourceHandler, times(1)).postChangedResource(any(), changedResourceCaptor.capture());
        verify(privateChangedResourcePost, times(1)).execute();
        Assertions.assertThat(changedResourceCaptor.getValue().getEvent().getType()).isEqualTo(EVENT_TYPE_DELETED);
        Assertions.assertThat(changedResourceCaptor.getValue().getDeletedData()).isInstanceOf(SuperSecure.class);
    }

    @Test
    void invokeChsKafkaEndpointWithDeleteForSuperSecureBO() throws ApiErrorResponseException, JsonProcessingException {
        // given
        when(internalApiClient.privateChangedResourceHandler()).thenReturn(privateChangedResourceHandler);
        when(privateChangedResourceHandler.postChangedResource(any(), any())).thenReturn(privateChangedResourcePost);
        when(privateChangedResourcePost.execute()).thenReturn(response);

        PscDocument document = TestHelper.buildPscDocument(TestHelper.SECURE_BO_KIND);
        SuperSecureBeneficialOwner superSecureBO = new SuperSecureBeneficialOwner();
        superSecureBO.setKind(SuperSecureBeneficialOwner.KindEnum.SUPER_SECURE_BENEFICIAL_OWNER);
        when(companyPscTransformer.transformPscDocToSuperSecureBeneficialOwner(
                document)).thenReturn(superSecureBO);

        when(objectMapper.writeValueAsString(superSecureBO)).thenReturn(superSecureBO.toString());
        when(objectMapper.readValue(superSecureBO.toString(), Object.class)).thenReturn(superSecureBO);

        // when
        ApiResponse<?> apiResponse = chsKafkaApiService.invokeChsKafkaApiWithDeleteEvent(
                TestHelper.X_REQUEST_ID, TestHelper.COMPANY_NUMBER, TestHelper.NOTIFICATION_ID, TestHelper.SECURE_BO_KIND,
                document);
        Assertions.assertThat(apiResponse).isNotNull();

        //then
        verify(internalApiClient, times(1)).privateChangedResourceHandler();
        verify(privateChangedResourceHandler, times(1)).postChangedResource(any(), changedResourceCaptor.capture());
        verify(privateChangedResourcePost, times(1)).execute();
        Assertions.assertThat(changedResourceCaptor.getValue().getEvent().getType()).isEqualTo(EVENT_TYPE_DELETED);
        Assertions.assertThat(changedResourceCaptor.getValue().getDeletedData()).isInstanceOf(SuperSecureBeneficialOwner.class);
    }

    @Test
    void invokeChsKafkaEndpointWithDeleteForCorporateEntity() throws ApiErrorResponseException, JsonProcessingException {
        // given
        when(internalApiClient.privateChangedResourceHandler()).thenReturn(privateChangedResourceHandler);
        when(privateChangedResourceHandler.postChangedResource(any(), any())).thenReturn(privateChangedResourcePost);
        when(privateChangedResourcePost.execute()).thenReturn(response);

        PscDocument document = TestHelper.buildPscDocument(TestHelper.CORPORATE_KIND);
        CorporateEntity corporateEntity = new CorporateEntity();
        corporateEntity.setKind(CorporateEntity.KindEnum.CORPORATE_ENTITY_PERSON_WITH_SIGNIFICANT_CONTROL);
        corporateEntity.setName("Corporate-Entity-Person-With-Significant-Control");
        when(companyPscTransformer.transformPscDocToCorporateEntity(document)).thenReturn(corporateEntity);

        when(objectMapper.writeValueAsString(corporateEntity)).thenReturn(corporateEntity.toString());
        when(objectMapper.readValue(corporateEntity.toString(), Object.class)).thenReturn(corporateEntity);

        // when
        ApiResponse<?> apiResponse = chsKafkaApiService.invokeChsKafkaApiWithDeleteEvent(
                TestHelper.X_REQUEST_ID, TestHelper.COMPANY_NUMBER, TestHelper.NOTIFICATION_ID, TestHelper.CORPORATE_KIND,
                document);
        Assertions.assertThat(apiResponse).isNotNull();

        //then
        verify(internalApiClient, times(1)).privateChangedResourceHandler();
        verify(privateChangedResourceHandler, times(1)).postChangedResource(any(), changedResourceCaptor.capture());
        verify(privateChangedResourcePost, times(1)).execute();
        Assertions.assertThat(changedResourceCaptor.getValue().getEvent().getType()).isEqualTo(EVENT_TYPE_DELETED);
        Assertions.assertThat(changedResourceCaptor.getValue().getDeletedData()).isInstanceOf(CorporateEntity.class);
    }

    @Test
    void invokeChsKafkaEndpointWithDeleteForCorporateEntityBO() throws ApiErrorResponseException, JsonProcessingException {
        // given
        when(internalApiClient.privateChangedResourceHandler()).thenReturn(privateChangedResourceHandler);
        when(privateChangedResourceHandler.postChangedResource(any(), any())).thenReturn(privateChangedResourcePost);
        when(privateChangedResourcePost.execute()).thenReturn(response);

        PscDocument document = TestHelper.buildPscDocument(TestHelper.CORPORATE_BO_KIND);
        CorporateEntityBeneficialOwner corporateEntityBO = new CorporateEntityBeneficialOwner();
        corporateEntityBO.setKind(CorporateEntityBeneficialOwner.KindEnum.CORPORATE_ENTITY_BENEFICIAL_OWNER);
        corporateEntityBO.setName("Corporate-Entity-Beneficial-Owner");
        when(companyPscTransformer.transformPscDocToCorporateEntityBeneficialOwner(document)).thenReturn(corporateEntityBO);

        when(objectMapper.writeValueAsString(corporateEntityBO)).thenReturn(corporateEntityBO.toString());
        when(objectMapper.readValue(corporateEntityBO.toString(), Object.class)).thenReturn(corporateEntityBO);

        // when
        ApiResponse<?> apiResponse = chsKafkaApiService.invokeChsKafkaApiWithDeleteEvent(
                TestHelper.X_REQUEST_ID, TestHelper.COMPANY_NUMBER, TestHelper.NOTIFICATION_ID, TestHelper.CORPORATE_BO_KIND,
                document);
        Assertions.assertThat(apiResponse).isNotNull();

        //then
        verify(internalApiClient, times(1)).privateChangedResourceHandler();
        verify(privateChangedResourceHandler, times(1)).postChangedResource(any(), changedResourceCaptor.capture());
        verify(privateChangedResourcePost, times(1)).execute();
        Assertions.assertThat(changedResourceCaptor.getValue().getEvent().getType()).isEqualTo(EVENT_TYPE_DELETED);
        Assertions.assertThat(changedResourceCaptor.getValue().getDeletedData()).isInstanceOf(CorporateEntityBeneficialOwner.class);
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
        Assertions.assertThat(changedResourceCaptor.getValue().getEvent().getType()).isEqualTo(EVENT_TYPE_CHANGED);
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
        Assertions.assertThat(changedResourceCaptor.getValue().getEvent().getType()).isEqualTo(EVENT_TYPE_DELETED);
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
        Assertions.assertThat(changedResourceCaptor.getValue().getEvent().getType()).isEqualTo(EVENT_TYPE_CHANGED);
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
        Assertions.assertThat(changedResourceCaptor.getValue().getEvent().getType()).isEqualTo(EVENT_TYPE_DELETED);
    }
}
