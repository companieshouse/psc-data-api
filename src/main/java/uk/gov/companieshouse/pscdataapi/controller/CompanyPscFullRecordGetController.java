package uk.gov.companieshouse.pscdataapi.controller;

import static uk.gov.companieshouse.pscdataapi.PscDataApiApplication.APPLICATION_NAME_SPACE;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.psc.IndividualFullRecord;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.pscdataapi.logging.DataMapHolder;
import uk.gov.companieshouse.pscdataapi.service.CompanyPscService;

@RestController
@ConditionalOnProperty(prefix = "feature", name = "identity_verification", havingValue = "true")
public class CompanyPscFullRecordGetController {

    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);

    private final CompanyPscService pscService;

    public CompanyPscFullRecordGetController(final CompanyPscService pscService) {
        this.pscService = pscService;
    }

    @GetMapping("/company/{company_number}/persons-with-significant-control/individual/{notification_id}/full_record")
    public ResponseEntity<IndividualFullRecord> getIndividualFullRecordPscData(
            @PathVariable("company_number") final String companyNumber,
            @PathVariable("notification_id") final String notificationId,
            final HttpServletRequest request) {

        DataMapHolder.get()
                .companyNumber(companyNumber)
                .itemId(notificationId);
        LOGGER.info("Getting full PSC record", DataMapHolder.getLogMap());
        final IndividualFullRecord individualFullRecord = pscService.getIndividualFullRecord(companyNumber,
                notificationId);

        LOGGER.info("Successfully retrieved full PSC record", DataMapHolder.getLogMap());
        return ResponseEntity.ok(individualFullRecord);
    }

}
