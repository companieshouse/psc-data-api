package uk.gov.companieshouse.pscdataapi.controller;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.model.psc.PscIndividualWithVerificationStateApi;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.pscdataapi.exceptions.ResourceNotFoundException;
import uk.gov.companieshouse.pscdataapi.logging.DataMapHolder;
import uk.gov.companieshouse.pscdataapi.service.CompanyPscService;

@RestController
@ConditionalOnProperty(prefix = "feature", name = "identity_verification", havingValue = "true")
@RequestMapping(path = "/company/{company_number}/persons-with-significant-control",
        produces = "application/json")
public class CompanyPscWithVerificationStateGetController {
    private static final Logger LOGGER = LoggerFactory.getLogger("psc-data-api");
    private static final String GETTING_PSC_DATA_WITH_VERIFICATION_STATE_WITH_COMPANY_NUMBER =
            "Getting PSC data + verification state with company number %s";

    private final CompanyPscService pscService;

    public CompanyPscWithVerificationStateGetController(final CompanyPscService pscService) {
        this.pscService = pscService;
    }

    /**
     * Get the data object with verification state for a company profile number for
     * Individual PSC.
     *
     * @param companyNumber The number of the company
     * @param notificationId The PSC notification ID
     * @return ResponseEntity
     */
    @GetMapping("/individual/{notification_id}/verification-state")
    public ResponseEntity<PscIndividualWithVerificationStateApi> getIndividualPscDataWithVerificationState(
            @PathVariable("company_number") String companyNumber,
            @PathVariable("notification_id") String notificationId) {
        DataMapHolder.get()
                .companyNumber(companyNumber)
                .itemId(notificationId);
        LOGGER.info(String.format(GETTING_PSC_DATA_WITH_VERIFICATION_STATE_WITH_COMPANY_NUMBER, companyNumber),
                DataMapHolder.getLogMap());
        try {
            PscIndividualWithVerificationStateApi individualWithVerificationState = pscService
                    .getIndividualWithVerificationState(companyNumber, notificationId);
            return ResponseEntity.ok(individualWithVerificationState);
        } catch (ResourceNotFoundException ex) {
            LOGGER.error(ex.getMessage(), DataMapHolder.getLogMap());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (DataAccessException ex) {
            LOGGER.error(ex.getMessage(), DataMapHolder.getLogMap());
            return ResponseEntity.internalServerError().build();
        }
    }
}
