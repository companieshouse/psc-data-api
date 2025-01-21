package uk.gov.companieshouse.pscdataapi.service;

import static uk.gov.companieshouse.pscdataapi.PscDataApiApplication.APPLICATION_NAME_SPACE;

import java.util.Optional;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
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
                throw new BadGatewayException("Error calling Company Exemptions API endpoint", ex);
            }
        } catch (URIValidationException ex) {
            LOGGER.info("URI validation error when calling Company Exemptions API", DataMapHolder.getLogMap());
            throw new BadGatewayException("URI validation error when calling Company Exemptions API", ex);
        }

        return response != null ? Optional.of(response.getData()) : Optional.empty();
    }
}
