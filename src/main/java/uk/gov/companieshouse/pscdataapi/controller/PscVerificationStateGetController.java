package uk.gov.companieshouse.pscdataapi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.model.psc.PscVerificationStateApi;
import uk.gov.companieshouse.api.model.psc.VerificationStateCriteriaApi;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.pscdataapi.exceptions.ResourceNotFoundException;
import uk.gov.companieshouse.pscdataapi.logging.DataMapHolder;
import uk.gov.companieshouse.pscdataapi.service.VerificationStateApiService;

@RestController
@RequestMapping(path = "/persons-of-significant-control", produces = "application/json")
public class PscVerificationStateGetController {
    private static final Logger LOGGER = LoggerFactory.getLogger("psc-data-api");

    private final VerificationStateApiService pscService;

    public PscVerificationStateGetController(final VerificationStateApiService pscService) {
        this.pscService = pscService;
    }

    /**
     * Get the individual full record data object (including sensitive data) for a company profile number for Individual
     * PSC.
     *
     * @return ResponseEntity
     */
    @PostMapping(value = "/verification-state", consumes = "application/json")
    public ResponseEntity<PscVerificationStateApi> getPscVerificationState(
        @RequestBody final VerificationStateCriteriaApi criteria) {
        try {
            final var verificationState = pscService.getPscVerificationState(criteria.appointmentId());
            return ResponseEntity.of(verificationState);
        } catch (final ResourceNotFoundException ex) {
            LOGGER.error(ex.getMessage(), DataMapHolder.getLogMap());
            return ResponseEntity.notFound().build();
        }
    }

}
