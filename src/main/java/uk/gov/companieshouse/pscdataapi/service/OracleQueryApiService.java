package uk.gov.companieshouse.pscdataapi.service;

import static uk.gov.companieshouse.pscdataapi.PscDataApiApplication.APPLICATION_NAME_SPACE;

import java.util.Optional;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.psc.PscVerificationStateApi;
import uk.gov.companieshouse.api.model.psc.VerificationStateCriteriaApi;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.pscdataapi.exceptions.BadGatewayException;
import uk.gov.companieshouse.pscdataapi.logging.DataMapHolder;

@Component
public class OracleQueryApiService {

    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);

    private final Supplier<InternalApiClient> oracleQueryApiClientSupplier;

    public OracleQueryApiService(
            @Qualifier("oracleQueryApiClientSupplier") Supplier<InternalApiClient> oracleQueryApiClientSupplier) {
        this.oracleQueryApiClientSupplier = oracleQueryApiClientSupplier;
    }

    public Optional<PscVerificationStateApi> getPscVerificationState(final Long applicationId) {
        ApiResponse<PscVerificationStateApi> response = null;

        try {
            response = oracleQueryApiClientSupplier.get()
                    .privatePscResourceHandler()
                    .getPscVerificationState(
                            "/corporate-body-appointments/persons-of-significant-control/verification-state",
                            new VerificationStateCriteriaApi(applicationId))
                    .execute();
        } catch (ApiErrorResponseException ex) {
            final int statusCode = ex.getStatusCode();
            LOGGER.info("PSC Verification State API POST failed with status code [%s]".formatted(statusCode),
                    DataMapHolder.getLogMap());
            if (statusCode != 404) {
                throw new BadGatewayException("PSC Verification State API POST API endpoint", ex);
            }
        } catch (URIValidationException ex) {
            LOGGER.info("URI validation error when calling PSC Verification State API POST API", DataMapHolder.getLogMap());
            throw new BadGatewayException("URI validation error when calling PSC Verification State API POST API", ex);
        }

        return Optional.ofNullable(response).map(ApiResponse::getData);
    }
}