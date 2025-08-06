package uk.gov.companieshouse.pscdataapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpResponseException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.handler.psc.PrivatePscResourceHandler;
import uk.gov.companieshouse.api.handler.psc.request.IdentityVerificationDetailsGetAsPost;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.psc.IdentityVerificationDetailsApi;
import uk.gov.companieshouse.api.model.psc.IdentityVerificationDetailsCriteriaApi;
import uk.gov.companieshouse.pscdataapi.exceptions.BadGatewayException;

@ExtendWith(MockitoExtension.class)
class OracleQueryApiServiceTest {
    private static final LocalDate START_ON = LocalDate.parse("2025-06-12");
    private static final LocalDate END_ON = LocalDate.parse("9999-12-31");
    private static final LocalDate STATEMENT_DATE = LocalDate.parse("2025-06-01");
    private static final LocalDate STATEMENT_DUE_DATE = LocalDate.parse("2025-06-15");

    private static final String URL = "/corporate-body-appointments/persons-of-significant-control/identity-verification-details";
    private static final IdentityVerificationDetailsApi identityVerificationDetailsApi = new IdentityVerificationDetailsApi(
            START_ON, END_ON, STATEMENT_DATE, STATEMENT_DUE_DATE);
    private static final IdentityVerificationDetailsCriteriaApi identityVerificationDetailsCriteriaApi
            = new IdentityVerificationDetailsCriteriaApi(1L);
    private static final ApiResponse<IdentityVerificationDetailsApi> SUCCESS_RESPONSE
            = new ApiResponse<>(200, null, identityVerificationDetailsApi);

    @InjectMocks
    private OracleQueryApiService service;
    @Mock
    private Supplier<InternalApiClient> supplier;
    @Mock
    private InternalApiClient client;
    @Mock
    private PrivatePscResourceHandler privatePscResourceHandler;
    @Mock
    private IdentityVerificationDetailsGetAsPost privateIdentityVerificationDetailsGet;

    @Test
    void shouldGetIdentityVerificationDetails() throws Exception {
        // given
        when(supplier.get()).thenReturn(client);
        when(client.privatePscResourceHandler()).thenReturn(privatePscResourceHandler);
        when(privatePscResourceHandler.getIdentityVerificationDetails(URL, identityVerificationDetailsCriteriaApi))
                .thenReturn(privateIdentityVerificationDetailsGet);
        when(privateIdentityVerificationDetailsGet.execute()).thenReturn(SUCCESS_RESPONSE);

        final Optional<IdentityVerificationDetailsApi> expected = Optional.of(identityVerificationDetailsApi);

        // when
        final Optional<IdentityVerificationDetailsApi> actual = service.getIdentityVerificationDetails(1L);

        // then
        verify(privatePscResourceHandler).getIdentityVerificationDetails(URL, identityVerificationDetailsCriteriaApi);
        assertEquals(expected, actual);
    }

    @Test
    void shouldGetIdentityVerificationDetailsWithNullResponseAndReturnEmptyOptional() throws Exception {
        // given
        when(supplier.get()).thenReturn(client);
        when(client.privatePscResourceHandler()).thenReturn(privatePscResourceHandler);
        when(privatePscResourceHandler.getIdentityVerificationDetails(URL, identityVerificationDetailsCriteriaApi))
                .thenReturn(privateIdentityVerificationDetailsGet);
        when(privateIdentityVerificationDetailsGet.execute()).thenReturn(null);

        final Optional<IdentityVerificationDetailsApi> expected = Optional.empty();

        // when
        final Optional<IdentityVerificationDetailsApi> actual = service.getIdentityVerificationDetails(1L);

        // then
        verify(privatePscResourceHandler).getIdentityVerificationDetails(URL, identityVerificationDetailsCriteriaApi);
        assertEquals(expected, actual);
    }

    @Test
    void shouldContinueProcessingWhenApiRespondsWith404NotFound() throws Exception {
        // given
        when(supplier.get()).thenReturn(client);
        when(client.privatePscResourceHandler()).thenReturn(privatePscResourceHandler);
        when(privatePscResourceHandler.getIdentityVerificationDetails(URL, identityVerificationDetailsCriteriaApi))
                .thenReturn(privateIdentityVerificationDetailsGet);
        when(privateIdentityVerificationDetailsGet.execute()).thenThrow(buildApiErrorResponseException(404));

        // when
        final Optional<IdentityVerificationDetailsApi> actual = service.getIdentityVerificationDetails(1L);

        // then
        assertTrue(actual.isEmpty());
        verify(privatePscResourceHandler).getIdentityVerificationDetails(URL, identityVerificationDetailsCriteriaApi);
    }

    @ParameterizedTest
    @CsvSource({"400", "401", "403", "405", "410", "500", "503"})
    void shouldThrowBadGatewayExceptionWhenApiRespondsWithNon404ErrorCode(final int statusCode) throws Exception {
        // given
        when(supplier.get()).thenReturn(client);
        when(client.privatePscResourceHandler()).thenReturn(privatePscResourceHandler);
        when(privatePscResourceHandler.getIdentityVerificationDetails(URL, identityVerificationDetailsCriteriaApi))
                .thenReturn(privateIdentityVerificationDetailsGet);
        when(privateIdentityVerificationDetailsGet.execute()).thenThrow(buildApiErrorResponseException(statusCode));

        // when
        final Executable executable = () -> service.getIdentityVerificationDetails(1L);

        // then
        assertThrows(BadGatewayException.class, executable);
        verify(privatePscResourceHandler).getIdentityVerificationDetails(URL, identityVerificationDetailsCriteriaApi);
    }

    @Test
    void shouldThrowBadGatewayExceptionWhenURIValidationExceptionCaught() throws Exception {
        // given
        when(supplier.get()).thenReturn(client);
        when(client.privatePscResourceHandler()).thenReturn(privatePscResourceHandler);
        when(privatePscResourceHandler.getIdentityVerificationDetails(URL, identityVerificationDetailsCriteriaApi))
                .thenReturn(privateIdentityVerificationDetailsGet);
        when(privateIdentityVerificationDetailsGet.execute()).thenThrow(URIValidationException.class);

        // when
        final Executable executable = () -> service.getIdentityVerificationDetails(1L);
        // then
        assertThrows(BadGatewayException.class, executable);
        verify(privatePscResourceHandler).getIdentityVerificationDetails(URL, identityVerificationDetailsCriteriaApi);
    }

    private static ApiErrorResponseException buildApiErrorResponseException(final int statusCode) {
        final HttpResponseException.Builder builder = new HttpResponseException
                .Builder(statusCode, "", new HttpHeaders());
        return new ApiErrorResponseException(builder);
    }

}