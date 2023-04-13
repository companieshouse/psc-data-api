package uk.gov.companieshouse.pscdataapi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.psc.FullRecordCompanyPSCApi;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.pscdataapi.exceptions.ServiceUnavailableException;
import uk.gov.companieshouse.pscdataapi.service.CompanyPscService;


@RestController
@RequestMapping(
        path = "/company/{company_number}/persons-with-significant-control/"
        + "{notification_id}/full_record", produces = "application/json")
public class CompanyPscController {

    @Autowired
    CompanyPscService pscService;

    private static final Logger LOGGER = LoggerFactory.getLogger("psc-data-api");

    /**
     * PUT endpoint for PSC record
     * @param contextId context Id taken from header.
     * @param request request payload.
     * @return response.
     */
    @PutMapping(consumes = "application/json")
    public ResponseEntity<Void> submitPscData(@RequestHeader("x-request-id") String contextId,
                                              @RequestBody final FullRecordCompanyPSCApi request) {
        try {
            LOGGER.info("Payload received, inserting record...");
            pscService.insertPscRecord(contextId, request);
            return ResponseEntity.ok().build();
        } catch (ServiceUnavailableException exception) {
            LOGGER.info(exception.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
