package uk.gov.companieshouse.pscdataapi.controller;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.psc.FullRecordCompanyPSCApi;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.pscdataapi.exceptions.ResourceNotFoundException;
import uk.gov.companieshouse.pscdataapi.logging.DataMapHolder;
import uk.gov.companieshouse.pscdataapi.service.CompanyPscService;


@RestController
@ConditionalOnProperty(prefix = "feature", name = "psc_individual_full_record_get", havingValue = "true")
@RequestMapping(path = "/private/company/{company_number}/persons-with-significant-control",
        produces = "application/json")
public class CompanyPscFullRecordGetController {
    private static final Logger LOGGER = LoggerFactory.getLogger("psc-data-api");
    private static final String GETTING_FULL_RECORD_PSC_DATA_WITH_COMPANY_NUMBER = "Getting Full record PSC data with company number %s";

    private final CompanyPscService pscService;

    public CompanyPscFullRecordGetController(final CompanyPscService pscService) {
        this.pscService = pscService;
    }

    /**
     * Get the full record data object (including sensitive data) for a company profile number for Individual PSC.
     *
     * @param companyNumber The number of the company
     * @param notificationId The PSC notification ID
     * @return ResponseEntity
     */
    @GetMapping("/individual/{notification_id}/full_record")
    public ResponseEntity<FullRecordCompanyPSCApi> getIndividualFullRecordPscData(
            @PathVariable("company_number") final String companyNumber,
            @PathVariable("notification_id") final String notificationId) {
        DataMapHolder.get()
                .companyNumber(companyNumber)
                .itemId(notificationId);
        LOGGER.info(String.format(GETTING_FULL_RECORD_PSC_DATA_WITH_COMPANY_NUMBER, companyNumber),
                DataMapHolder.getLogMap());
        try {
            final FullRecordCompanyPSCApi fullRecordPsc = pscService.getFullRecordPsc(companyNumber, notificationId);
            return ResponseEntity.ok(fullRecordPsc);
        } catch (final ResourceNotFoundException ex) {
            LOGGER.error(ex.getMessage(), DataMapHolder.getLogMap());
            return ResponseEntity.notFound().build();
        } catch (final DataAccessException ex) {
            LOGGER.error(ex.getMessage(), DataMapHolder.getLogMap());
            return ResponseEntity.internalServerError().build();
        }
    }

}