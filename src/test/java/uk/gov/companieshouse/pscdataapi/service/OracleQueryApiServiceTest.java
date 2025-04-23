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
import uk.gov.companieshouse.api.handler.psc.request.PscVerificationStateGetAsPost;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.psc.PscVerificationStateApi;
import uk.gov.companieshouse.api.model.psc.VerificationStateCriteriaApi;
import uk.gov.companieshouse.api.model.psc.VerificationStatusTypeApi;
import uk.gov.companieshouse.pscdataapi.exceptions.BadGatewayException;

@ExtendWith(MockitoExtension.class)
class OracleQueryApiServiceTest {

    private static final String URL = "/corporate-body-appointments/persons-of-significant-control/verification-state";
    private static final PscVerificationStateApi pscVerificationStateApi = new PscVerificationStateApi(
            VerificationStatusTypeApi.VERIFIED,
            LocalDate.of(2025, 1, 10),
            LocalDate.of(2025, 2, 5)
    );
    private static final VerificationStateCriteriaApi verificationStateCriteriaApi
            = new VerificationStateCriteriaApi(1L);
    private static final ApiResponse<PscVerificationStateApi> SUCCESS_RESPONSE
            = new ApiResponse<>(200, null, pscVerificationStateApi);

    @InjectMocks
    private OracleQueryApiService service;
    @Mock
    private Supplier<InternalApiClient> supplier;
    @Mock
    private InternalApiClient client;
    @Mock
    private PrivatePscResourceHandler privatePscResourceHandler;
    @Mock
    private PscVerificationStateGetAsPost privatePscVerificationStateGet;

    @Test
    void shouldGetPscVerificationState() throws Exception {
        // given
        when(supplier.get()).thenReturn(client);
        when(client.privatePscResourceHandler()).thenReturn(privatePscResourceHandler);
        when(privatePscResourceHandler.getPscVerificationState(URL, verificationStateCriteriaApi))
                .thenReturn(privatePscVerificationStateGet);
        when(privatePscVerificationStateGet.execute()).thenReturn(SUCCESS_RESPONSE);

        final Optional<PscVerificationStateApi> expected = Optional.of(pscVerificationStateApi);

        // when
        final Optional<PscVerificationStateApi> actual = service.getPscVerificationState(1L);

        // then
        verify(privatePscResourceHandler).getPscVerificationState(URL, verificationStateCriteriaApi);
        assertEquals(expected, actual);
    }

    @Test
    void shouldGetPscVerificationStateWithNullResponseAndReturnEmptyOptional() throws Exception {
        // given
        when(supplier.get()).thenReturn(client);
        when(client.privatePscResourceHandler()).thenReturn(privatePscResourceHandler);
        when(privatePscResourceHandler.getPscVerificationState(URL, verificationStateCriteriaApi))
                .thenReturn(privatePscVerificationStateGet);
        when(privatePscVerificationStateGet.execute()).thenReturn(null);

        final Optional<PscVerificationStateApi> expected = Optional.empty();

        // when
        final Optional<PscVerificationStateApi> actual = service.getPscVerificationState(1L);

        // then
        verify(privatePscResourceHandler).getPscVerificationState(URL, verificationStateCriteriaApi);
        assertEquals(expected, actual);
    }

    @Test
    void shouldContinueProcessingWhenApiRespondsWith404NotFound() throws Exception {
        // given
        when(supplier.get()).thenReturn(client);
        when(client.privatePscResourceHandler()).thenReturn(privatePscResourceHandler);
        when(privatePscResourceHandler.getPscVerificationState(URL, verificationStateCriteriaApi))
                .thenReturn(privatePscVerificationStateGet);
        when(privatePscVerificationStateGet.execute()).thenThrow(buildApiErrorResponseException(404));

        // when
        final Optional<PscVerificationStateApi> actual = service.getPscVerificationState(1L);

        // then
        assertTrue(actual.isEmpty());
        verify(privatePscResourceHandler).getPscVerificationState(URL, verificationStateCriteriaApi);
    }

    @ParameterizedTest
    @CsvSource({"400", "401", "403", "405", "410", "500", "503"})
    void shouldThrowBadGatewayExceptionWhenApiRespondsWithNon404ErrorCode(final int statusCode) throws Exception {
        // given
        when(supplier.get()).thenReturn(client);
        when(client.privatePscResourceHandler()).thenReturn(privatePscResourceHandler);
        when(privatePscResourceHandler.getPscVerificationState(URL, verificationStateCriteriaApi))
                .thenReturn(privatePscVerificationStateGet);
        when(privatePscVerificationStateGet.execute()).thenThrow(buildApiErrorResponseException(statusCode));

        // when
        final Executable executable = () -> service.getPscVerificationState(1L);

        // then
        assertThrows(BadGatewayException.class, executable);
        verify(privatePscResourceHandler).getPscVerificationState(URL, verificationStateCriteriaApi);
    }

    @Test
    void shouldThrowBadGatewayExceptionWhenURIValidationExceptionCaught() throws Exception {
        // given
        when(supplier.get()).thenReturn(client);
        when(client.privatePscResourceHandler()).thenReturn(privatePscResourceHandler);
        when(privatePscResourceHandler.getPscVerificationState(URL, verificationStateCriteriaApi))
                .thenReturn(privatePscVerificationStateGet);
        when(privatePscVerificationStateGet.execute()).thenThrow(URIValidationException.class);

        // when
        final Executable executable = () -> service.getPscVerificationState(1L);
        // then
        assertThrows(BadGatewayException.class, executable);
        verify(privatePscResourceHandler).getPscVerificationState(URL, verificationStateCriteriaApi);
    }

    private static ApiErrorResponseException buildApiErrorResponseException(final int statusCode) {
        final HttpResponseException.Builder builder = new HttpResponseException
                .Builder(statusCode, "", new HttpHeaders());
        return new ApiErrorResponseException(builder);
    }

}