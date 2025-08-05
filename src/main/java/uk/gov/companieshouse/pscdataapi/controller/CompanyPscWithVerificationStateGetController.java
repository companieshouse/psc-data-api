package uk.gov.companieshouse.pscdataapi.controller;

import static uk.gov.companieshouse.pscdataapi.PscDataApiApplication.APPLICATION_NAME_SPACE;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.model.psc.PscIndividualWithVerificationStateApi;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.pscdataapi.logging.DataMapHolder;
import uk.gov.companieshouse.pscdataapi.service.CompanyPscService;

@RestController
@ConditionalOnProperty(prefix = "feature", name = "identity_verification", havingValue = "true")
public class CompanyPscWithVerificationStateGetController {

    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);

    private final CompanyPscService pscService;

    public CompanyPscWithVerificationStateGetController(final CompanyPscService pscService) {
        this.pscService = pscService;
    }

    @GetMapping("/company/{company_number}/persons-with-significant-control/individual/{notification_id}/identity-verification-details")
    public ResponseEntity<PscIndividualWithVerificationStateApi> getIndividualPscDataWithVerificationState(
            @PathVariable("company_number") String companyNumber,
            @PathVariable("notification_id") String notificationId) {
        DataMapHolder.get()
                .companyNumber(companyNumber)
                .itemId(notificationId);
        LOGGER.info("Getting PSC data and verification state", DataMapHolder.getLogMap());
        PscIndividualWithVerificationStateApi individualWithVerificationState = pscService
                .getIndividualWithVerificationState(companyNumber, notificationId);

        LOGGER.info("Successfully retrieved PSC data and verification state", DataMapHolder.getLogMap());
        return ResponseEntity.ok(individualWithVerificationState);
    }
}
