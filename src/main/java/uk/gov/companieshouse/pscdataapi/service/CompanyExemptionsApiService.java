package uk.gov.companieshouse.pscdataapi.service;

import static uk.gov.companieshouse.pscdataapi.PscDataApiApplication.APPLICATION_NAME_SPACE;

import java.util.Optional;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.exemptions.CompanyExemptions;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.pscdataapi.exceptions.BadGatewayException;
import uk.gov.companieshouse.pscdataapi.logging.DataMapHolder;

@Component
public class CompanyExemptionsApiService {

    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);

    private final Supplier<InternalApiClient> exemptionsApiClientSupplier;

    public CompanyExemptionsApiService(
            @Qualifier("exemptionsApiClientSupplier") Supplier<InternalApiClient> exemptionsApiClientSupplier) {
        this.exemptionsApiClientSupplier = exemptionsApiClientSupplier;
    }

    public Optional<CompanyExemptions> getCompanyExemptions(final String companyNumber) {
        ApiResponse<CompanyExemptions> response = null;
        try {
            response = exemptionsApiClientSupplier.get()
                    .privateDeltaResourceHandler()
                    .getCompanyExemptionsResource("/company/%s/exemptions".formatted(companyNumber))
                    .execute();
        } catch (ApiErrorResponseException ex) {
            final int statusCode = ex.getStatusCode();
            LOGGER.info("Company Exemptions API GET failed with status code [%s]".formatted(statusCode),
                    DataMapHolder.getLogMap());
            if (statusCode != 404) {
                final String msg = "Error calling Company Exemptions API endpoint";
                LOGGER.error(msg, DataMapHolder.getLogMap());
                throw new BadGatewayException(msg, ex);
            }
        } catch (URIValidationException ex) {
            final String msg = "URI validation error when calling Company Exemptions API";
            LOGGER.info(msg, DataMapHolder.getLogMap());
            throw new BadGatewayException(msg, ex);
        }

        return Optional.ofNullable(response)
                .map(ApiResponse::getData);
    }
}
