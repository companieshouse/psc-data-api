package uk.gov.companieshouse.pscdataapi.controller;

import static uk.gov.companieshouse.pscdataapi.PscDataApiApplication.APPLICATION_NAME_SPACE;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.model.psc.PscIndividualFullRecordApi;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.pscdataapi.interceptor.AuthenticationHelper;
import uk.gov.companieshouse.pscdataapi.logging.DataMapHolder;
import uk.gov.companieshouse.pscdataapi.service.CompanyPscService;

@RestController
@ConditionalOnProperty(prefix = "feature", name = "identity_verification", havingValue = "true")
public class CompanyPscFullRecordGetController {

    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);
    public static final String OAUTH_2 = "oauth2";

    private final CompanyPscService pscService;
    private final AuthenticationHelper authHelper;

    public CompanyPscFullRecordGetController(final CompanyPscService pscService, AuthenticationHelper authHelper) {
        this.pscService = pscService;
        this.authHelper = authHelper;
    }

    @GetMapping("/company/{company_number}/persons-with-significant-control/individual/{notification_id}/full_record")
    public ResponseEntity<PscIndividualFullRecordApi> getIndividualFullRecordPscData(
            @PathVariable("company_number") final String companyNumber,
            @PathVariable("notification_id") final String notificationId,
            final HttpServletRequest request) {

        final String identityType = authHelper.getAuthorisedIdentityType(request);
        DataMapHolder.get()
                .companyNumber(companyNumber)
                .itemId(notificationId);
        LOGGER.info("Getting full PSC record", DataMapHolder.getLogMap());
        final PscIndividualFullRecordApi individualFullRecord = pscService.getIndividualFullRecord(companyNumber,
                notificationId);
        if (identityType.equals(OAUTH_2)) {
            individualFullRecord.setInternalId(null);
        }

        LOGGER.info("Successfully retrieved full PSC record", DataMapHolder.getLogMap());
        return ResponseEntity.ok(individualFullRecord);
    }

}
