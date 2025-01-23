package uk.gov.companieshouse.pscdataapi.service;

import static uk.gov.companieshouse.pscdataapi.PscDataApiApplication.APPLICATION_NAME_SPACE;

import java.util.Optional;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.pscdataapi.exceptions.BadGatewayException;
import uk.gov.companieshouse.pscdataapi.logging.DataMapHolder;

@Component
public class CompanyMetricsApiService {

    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);

    private final Supplier<InternalApiClient> metricsApiClientSupplier;

    public CompanyMetricsApiService(
            @Qualifier("metricsApiClientSupplier") Supplier<InternalApiClient> meticsApiClientSupplier) {
        this.metricsApiClientSupplier = meticsApiClientSupplier;
    }

    public Optional<MetricsApi> getCompanyMetrics(final String companyNumber) {
        ApiResponse<MetricsApi> response = null;
        try {
            response = metricsApiClientSupplier.get()
                    .privateCompanyMetricsResourceHandler()
                    .getCompanyMetrics(String.format("/company/%s/metrics", companyNumber))
                    .execute();
        } catch (ApiErrorResponseException ex) {
            final int statusCode = ex.getStatusCode();
            LOGGER.info("Company Metrics API call failed with status code [%s]".formatted(statusCode),
                    DataMapHolder.getLogMap());
            if (statusCode != 404) {
                throw new BadGatewayException("Error calling Company Metrics API endpoint", ex);
            }
        } catch (URIValidationException ex) {
            LOGGER.info("URI validation error when calling Company Metrics API", DataMapHolder.getLogMap());
            throw new BadGatewayException("URI validation error when calling Company Metrics API", ex);
        }

        return Optional.ofNullable(response)
                .map(ApiResponse::getData);
    }
}
